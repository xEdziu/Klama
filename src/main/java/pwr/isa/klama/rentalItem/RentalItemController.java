package pwr.isa.klama.rentalItem;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import pwr.isa.klama.rentalItem.rent.RentRecordDTO;
import pwr.isa.klama.rentalItem.rent.RentRequest;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(path = "api/v1")
public class RentalItemController {

    private final RentalItemService rentalItemService;

    @Autowired
    public RentalItemController(RentalItemService rentalItemService) {
        this.rentalItemService = rentalItemService;
    }

    // ============ Freely available methods ============
    @GetMapping(path = "/rentalItem/all")
    public List<RentalItem> getRentalItems() {
        return rentalItemService.getRentalItem();
    }

    @GetMapping(path = "/rentalItem/{rentalItemId}")
    public RentalItem getRentalItem(@PathVariable("rentalItemId") Long rentalItemId) {
        return rentalItemService.getRentalItemById(rentalItemId);
    }
    // ============ User methods ============
    @GetMapping(path = "/authorized/rentalItem/rentHistory")
    public List<RentRecordDTO> getRentHistory() { return rentalItemService.getRentHistory(); }

    @PostMapping(path = "/authorized/rentalItem/rent")
    //TODO: Dorobić uwzględznianie ceny zależnie od ilości dni
    //TODO: Dorabić zaciąganie odpowiednich dat
    public Map<String, Object> rentRentalItem(@RequestBody List<RentRequest> rentRequests) {
        return rentalItemService.rentRentalItems(rentRequests);
    }

    //TODO: funkcja zwrotu przedmiotu

    // ============ Admin methods ============
    @GetMapping(path = "/authorized/admin/rentalItem/history")
    public List<RentRecordDTO> getRentHistoryAll() { return rentalItemService.getRentHistoryAll();
    }

    @PostMapping(path = "/authorized/admin/rentalItem/add")
    public Map<String, Object> addNewRentalItem(@RequestBody RentalItem rentalItem) {
        return rentalItemService.addNewRentalItem(rentalItem);
    }

    @DeleteMapping(path = "/authorized/admin/rentalItem/delete/{rentalItemId}")
    //TODO: Nie da się usunąć przedmiotu, który jest wypożyczony, trzeba zwrócić na to uwagę bo wywala 500-tke zamiast ładnego komunikatu
    public Map<String, Object> deleteRentalItem(@PathVariable("rentalItemId") Long rentalItemID) {
        return rentalItemService.deleteRentalItem(rentalItemID);
    }

    @PutMapping(path = "/authorized/admin/rentalItem/update/{rentalItemId}")
    public Map<String, Object> updateRentalItem(
            @PathVariable("rentalItemId") Long rentalItemId,
            @RequestBody RentalItem rentalItem) {
         return rentalItemService.updateRentalItem(rentalItemId, rentalItem);
    }
}
