package pwr.isa.klama.pass;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import pwr.isa.klama.exceptions.ResourceNotFoundException;
import pwr.isa.klama.pass.userPass.*;
import pwr.isa.klama.rentalItem.ItemStatus;
import pwr.isa.klama.rentalItem.RentalItem;
import pwr.isa.klama.user.User;

import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PassService {

    private final PassRepository passRepository;
    private final UserPassRepository userPassRepository;

    @Autowired
    public PassService(PassRepository passRepository, UserPassRepository userPassRepository) {
        this.passRepository = passRepository;
        this.userPassRepository = userPassRepository;
    }

    public List<Pass> getPassesForAdmin() {
        return passRepository.findAll();
    }

    public List<PassDTO> getPasses() {
        return passRepository.findAll().stream()
                .filter(pass -> pass.getStatus() == PassStatus.VISIBLE)
                .map(pass -> new PassDTO(
                        pass.getId(),
                        pass.getName(),
                        pass.getDescription(),
                        pass.getPrice(),
                        pass.getDays()
                ))
                .collect(Collectors.toList());
    }

    public Pass getPassById(Long passId) {
        return passRepository.findById(passId)
                .orElseThrow(() -> new ResourceNotFoundException("Karnet o id " + passId + " nie istnieje"));
    }

    public List<UserPassHistoryDTO> getPassHistory() {
        Long userId = getCurrentUser().getId();
        return userPassRepository.findByUserId(userId).stream()
                .peek(this::updatePassStatus)
                .map(userPass -> new UserPassHistoryDTO(
                        userPass.getPass().getId(),
                        userPass.getPass().getName(),
                        userPass.getBuyDate(),
                        userPass.getExpirationDate(),
                        userPass.getPass().getPrice()
                ))
                .collect(Collectors.toList());
    }

    public List<UserPassDTO> getPassHistoryAll() {
        return userPassRepository.findAll().stream()
                .peek(this::updatePassStatus)
                .map(userPass -> new UserPassDTO(
                        userPass.getId(),
                        userPass.getUser().getId(),
                        userPass.getPass().getId(),
                        userPass.getStatus().toString(),
                        userPass.getBuyDate().toString(),
                        userPass.getExpirationDate().toString()
                ))
                .collect(Collectors.toList());
    }

    @Transactional
    public Map<String, Object> buyNewPass(Long passId) {
        Map<String, Object> response = new HashMap<>();
        Timestamp buyDate = new Timestamp(new Date().getTime());

        Pass pass = passRepository.findById(passId)
                .orElseThrow(() -> new ResourceNotFoundException("Karnet o id " + passId + " nie istnieje"));

        if (pass.getStatus() != PassStatus.VISIBLE) {
            throw new IllegalStateException("Karnet o id " + passId + " nie jest możliwy do zakupu");
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(buyDate);
        calendar.add(Calendar.DAY_OF_YEAR, pass.getDays());
        Timestamp expirationDate = new Timestamp(calendar.getTimeInMillis());

        User user = getCurrentUser();
        UserPass userPass = new UserPass(
                user,
                pass,
                UserPassStatus.ACTIVE,
                buyDate,
                expirationDate
        );
        userPassRepository.save(userPass);
        response.put("message", "Kupno karnetu zakończone sukcesem");
        response.put("error", HttpStatus.OK.value());
        response.put("timestamp", new Timestamp(new Date().getTime()));
        return response;
    }

    public Map<String, Object> addPass(Pass pass) {
        Optional<Pass> passOptional = passRepository.findPassByName(pass.getName());
        if (passOptional.isPresent()) {
            throw new IllegalStateException("Przedmiot o nazwie: " + pass.getName() + " już istnieje");
        }
        passRepository.save(pass);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Dodanie karnetu zakończone sukcesem");
        response.put("error", HttpStatus.OK.value());
        response.put("timestamp", new Timestamp(new Date().getTime()));
        return response;
    }

    public Map<String, Object> deletePass(Long passId) {
        boolean exists = passRepository.existsById(passId);
        if( !exists ) {
            throw new ResourceNotFoundException("Karnet o id " + passId + " nie istnieje");
        }

        String message;
        // Sprawdzenie, czy karnet jest wypożyczony
        if(userPassRepository.existsByPass_Id(passId)){
            message = "Karnet o id " + passId + " jest powiązany z userem i nie można go usunąć, karnet ustawiono jako ukryty";
            getPassById(passId).setStatus(PassStatus.HIDDEN);
            passRepository.save(getPassById(passId));
        } else {
            message = "Przedmiot o id " + passId + " został usunięty";
            passRepository.deleteById(passId);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("message", message);
        response.put("error", HttpStatus.OK.value());
        response.put("timestamp", new Timestamp(new Date().getTime()));
        return response;
    }

    @Transactional
    public Map<String, Object> disablePass(Long passId) {
        boolean exists = passRepository.existsById(passId);
        if( !exists ) {
            throw new ResourceNotFoundException("Karnet o id " + passId + " nie istnieje");
        }

        Pass pass = getPassById(passId);
        pass.setStatus(PassStatus.HIDDEN);
        passRepository.save(pass);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Karnet o id " + passId + " został ukryty");
        response.put("error", HttpStatus.OK.value());
        response.put("timestamp", new Timestamp(new Date().getTime()));
        return response;
    }

    @Transactional
    public Map<String, Object> updatePass(Pass pass, Long passId) {
        if(pass.getName() == null &&
                pass.getId() == null &&
                pass.getPrice() == null &&
                pass.getDays() == null &&
                pass.getDescription() == null &&
                pass.getStatus() == null) {
            throw new IllegalStateException("Należy podać jeden z parametrów");
        }
        if(!passRepository.existsById(passId)) {
            throw new ResourceNotFoundException("Karnet o id " + passId + " nie istnieje");
        }

        Pass passToUpdate = passRepository.findById(passId)
                .orElseThrow(() -> new ResourceNotFoundException("Karnet o id " + passId + " nie istnieje"));

        if(pass.getName() != null) {
            if(passToUpdate.getName().isEmpty()) {
                throw new IllegalStateException("Nazwa nie może być pusta");
            }
            passToUpdate.setName(pass.getName());
        }

        if(pass.getPrice() != null) {
            if(passToUpdate.getPrice() == null) {
                throw new IllegalStateException("Cena nie może być pusta");
            }
            passToUpdate.setPrice(pass.getPrice());
        }

        if(pass.getDays() != null) {
            if(passToUpdate.getDays() == null) {
                throw new IllegalStateException("Ilość dni nie może być pusta");
            }
            passToUpdate.setDays(pass.getDays());
        }

        if(pass.getDescription() != null) {
            if(passToUpdate.getDescription().isEmpty()) {
                throw new IllegalStateException("Opis nie może być pusty");
            }
            passToUpdate.setDescription(pass.getDescription());
        }

        if(pass.getStatus() != null) {
            if(passToUpdate.getStatus() == null) {
                throw new IllegalStateException("Status nie może być pusty");
            }
            passToUpdate.setStatus(pass.getStatus());
        }

        passRepository.save(passToUpdate);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Karnet o id " + passId + " został zaktualizowany");
        response.put("error", HttpStatus.OK.value());
        response.put("timestamp", new Timestamp(new Date().getTime()));
        return response;
    }

    private User getCurrentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    private void updatePassStatus(UserPass userPass) {
        if (userPass.getStatus() == UserPassStatus.ACTIVE && userPass.getExpirationDate().before(new Date())) {
            userPass.setStatus(UserPassStatus.EXPIRED);
            userPassRepository.save(userPass);
        }
    }
}
