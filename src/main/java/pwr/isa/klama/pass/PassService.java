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
import pwr.isa.klama.security.logging.ApiLogger;
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
        ApiLogger.logInfo("/api/v1/authorized/admin/pass/all","Getting all passes for admin");
        return passRepository.findAll();
    }

    public List<PassDTO> getPasses() {
        ApiLogger.logInfo("/api/v1/pass/all","Getting all passes");
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
        ApiLogger.logInfo("/api/v1/pass/" + passId,"Getting pass: " + passId);
        return passRepository.findById(passId)
                .orElseThrow(() -> {
                    ApiLogger.logSevere("/api/v1/pass/" + passId, "Pass " + passId + " not found");
                    return new ResourceNotFoundException("Karnet o id " + passId + " nie istnieje");
                });
    }

    public List<UserPassHistoryDTO> getPassHistory() {
        Long userId = getCurrentUser().getId();
        ApiLogger.logInfo("/api/v1/authorized/pass/passHistory","Getting passHistory for user: " + userId);
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
        ApiLogger.logInfo("/api/v1/authorized/admin/pass/passHistory/all","Getting all users passHistory for Admin");
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

    public Map<String, Object> buyNewPass(Long passId) {
        Map<String, Object> response = new HashMap<>();
        Long userId = getCurrentUser().getId();
        ApiLogger.logInfo("/api/v1/authorized/pass/buyNew/" + passId,"Buying new pass " + passId  + " for user: " + userId);

        Timestamp buyDate = new Timestamp(new Date().getTime());

        Pass pass = passRepository.findById(passId)
                .orElseThrow(() -> {
                    ApiLogger.logSevere("/api/v1/authorized/pass/buyNew/" + passId, "Pass " + passId + " not found");
                    return new ResourceNotFoundException("Karnet o id " + passId + " nie istnieje");
                });

        if (pass.getStatus() != PassStatus.VISIBLE) {
            ApiLogger.logInfo("/api/v1/authorized/pass/buyNew/" + passId,"Pass " +  + passId + " is not visible to buy. ");
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

        ApiLogger.logInfo("/api/v1/authorized/pass/buyNew/" + passId, "Pass " + passId + " bought successfully for user: " + userId);
        return response;
    }

    public Map<String, Object> addPass(Pass pass) {
        Optional<Pass> passOptional = passRepository.findPassByName(pass.getName());
        ApiLogger.logInfo("/api/v1/authorized/admin/pass/add","Adding new pass: " + pass.getName());
        if (passOptional.isPresent()) {
            ApiLogger.logWarning("/api/v1/authorized/admin/pass/add", "Pass with name " + pass.getName() + " already exists");
            throw new IllegalStateException("Przedmiot o nazwie: " + pass.getName() + " już istnieje");
        }
        passRepository.save(pass);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Dodanie karnetu zakończone sukcesem");
        response.put("error", HttpStatus.OK.value());
        response.put("timestamp", new Timestamp(new Date().getTime()));

        ApiLogger.logInfo("/api/v1/authorized/admin/pass/add", "Pass " + pass.getName() + " added successfully");
        return response;
    }

    public Map<String, Object> deletePass(Long passId) {
        boolean exists = passRepository.existsById(passId);
        if (!exists) {
            ApiLogger.logSevere("/api/v1/authorized/admin/pass/delete/" + passId, "Pass " + passId + " not found");
            throw new ResourceNotFoundException("Karnet o id " + passId + " nie istnieje");
        }

        String message;
        // Sprawdzenie, czy karnet jest wypożyczony
        if(userPassRepository.existsByPass_Id(passId)){
            message = "Karnet o id " + passId + " jest powiązany z userem i nie można go usunąć, karnet ustawiono jako ukryty";
            getPassById(passId).setStatus(PassStatus.HIDDEN);
            passRepository.save(getPassById(passId));
            ApiLogger.logInfo("/api/v1/authorized/admin/pass/delete/" + passId,"Deleting impossible, Hiding pass: " + passId);
        } else {
            message = "Przedmiot o id " + passId + " został usunięty";
            passRepository.deleteById(passId);
            ApiLogger.logInfo("/api/v1/authorized/admin/pass/delete/" + passId,"Deleting pass: " + passId);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("message", message);
        response.put("error", HttpStatus.OK.value());
        response.put("timestamp", new Timestamp(new Date().getTime()));
        return response;
    }

    public Map<String, Object> disablePass(Long passId) {
        boolean exists = passRepository.existsById(passId);
        if (!exists) {
            ApiLogger.logSevere("/api/v1/authorized/admin/pass/disable/" + passId, "Pass " + passId + " not found");
            throw new ResourceNotFoundException("Karnet o id " + passId + " nie istnieje");
        }

        Pass pass = getPassById(passId);
        pass.setStatus(PassStatus.HIDDEN);
        passRepository.save(pass);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Karnet o id " + passId + " został ukryty");
        response.put("error", HttpStatus.OK.value());
        response.put("timestamp", new Timestamp(new Date().getTime()));

        ApiLogger.logInfo("/api/v1/authorized/admin/pass/disable/" + passId, "Pass " + passId + " disabled successfully");
        return response;
    }

    public Map<String, Object> updatePass(Pass pass, Long passId) {
        if(pass.getName() == null &&
                pass.getId() == null &&
                pass.getPrice() == null &&
                pass.getDays() == null &&
                pass.getDescription() == null &&
                pass.getStatus() == null) {
            ApiLogger.logWarning("/api/v1/authorized/admin/pass/update/" + passId, "No parameters provided for update");
            throw new IllegalStateException("Należy podać jeden z parametrów");
        }
        if (!passRepository.existsById(passId)) {
            ApiLogger.logSevere("/api/v1/authorized/admin/pass/update/" + passId, "Pass " + passId + " not found");
            throw new ResourceNotFoundException("Karnet o id " + passId + " nie istnieje");
        }

        Pass passToUpdate = passRepository.findById(passId)
                .orElseThrow(() -> new ResourceNotFoundException("Karnet o id " + passId + " nie istnieje"));

        if(pass.getName() != null) {
            if(passToUpdate.getName().isEmpty()) {
                ApiLogger.logWarning("/api/v1/authorized/admin/pass/update/" + passId, "Pass name cannot be empty");
                throw new IllegalStateException("Nazwa nie może być pusta");
            }
            passToUpdate.setName(pass.getName());
        }

        if(pass.getPrice() != null) {
            if(passToUpdate.getPrice() == null) {
                ApiLogger.logWarning("/api/v1/authorized/admin/pass/update/" + passId, "Pass price cannot be empty");
                throw new IllegalStateException("Cena nie może być pusta");
            }
            passToUpdate.setPrice(pass.getPrice());
        }

        if(pass.getDays() != null) {
            if(passToUpdate.getDays() == null) {
                ApiLogger.logWarning("/api/v1/authorized/admin/pass/update/" + passId, "Pass days cannot be empty");
                throw new IllegalStateException("Ilość dni nie może być pusta");
            }
            passToUpdate.setDays(pass.getDays());
        }

        if(pass.getDescription() != null) {
            if(passToUpdate.getDescription().isEmpty()) {
                ApiLogger.logWarning("/api/v1/authorized/admin/pass/update/" + passId, "Pass description cannot be empty");
                throw new IllegalStateException("Opis nie może być pusty");
            }
            passToUpdate.setDescription(pass.getDescription());
        }

        if(pass.getStatus() != null) {
            if(passToUpdate.getStatus() == null) {
                ApiLogger.logWarning("/api/v1/authorized/admin/pass/update/" + passId, "Pass status cannot be empty");
                throw new IllegalStateException("Status nie może być pusty");
            }
            passToUpdate.setStatus(pass.getStatus());
        }

        passRepository.save(passToUpdate);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Karnet o id " + passId + " został zaktualizowany");
        response.put("error", HttpStatus.OK.value());
        response.put("timestamp", new Timestamp(new Date().getTime()));

        ApiLogger.logInfo("/api/v1/authorized/admin/pass/update/" + passId, "Pass " + passId + " updated successfully");
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
