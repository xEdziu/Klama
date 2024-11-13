package pwr.isa.klama.rentalItem;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import pwr.isa.klama.rentalItem.rent.RentRecordDTO;
import pwr.isa.klama.rentalItem.rent.RentRequest;
import pwr.isa.klama.rentalItem.rent.RentRequestBody;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    public List<RentalItemDTO> getRentalItems() {
        return rentalItemService.getRentalItems();
    }

    @GetMapping(path = "/rentalItem/{rentalItemId}")
    public RentalItem getRentalItem(@PathVariable("rentalItemId") Long rentalItemId) {
        return rentalItemService.getRentalItemById(rentalItemId);
    }
    // ============ User methods ============
    @GetMapping(path = "/authorized/rentalItem/rentHistory")
    public List<RentRecordDTO> getRentHistory() { return rentalItemService.getRentHistory(); }

    @PostMapping(path = "/authorized/rentalItem/rent")
    public Map<String, Object> rentRentalItems(@RequestBody RentRequestBody rentRequestBody) {
        return rentalItemService.rentRentalItems(rentRequestBody.getRentRequests(), rentRequestBody.getRentDate(), rentRequestBody.getReturnDate());
    }

    @PostMapping(path = "/authorized/rentalItem/return/{rentId}")
    public Map<String, Object> returnRentalItem(@PathVariable("rentId") Long rentId) {
        return rentalItemService.returnRentalItems(rentId);
    }

    // ============ Admin methods ============
    @GetMapping(path = "/authorized/admin/rentalItem/all")
    public List<RentalItem> getRentalItemsForAdmin() {
        return rentalItemService.getRentalItemsForAdmin();
    }

    @GetMapping(path = "/authorized/admin/rentalItem/history")
    public List<RentRecordDTO> getRentHistoryAll() { return rentalItemService.getRentHistoryAll();
    }

    @PostMapping(path = "/authorized/admin/rentalItem/add")
    public Map<String, Object> addNewRentalItem(@RequestBody RentalItem rentalItem) {
        return rentalItemService.addNewRentalItem(rentalItem);
    }

    @DeleteMapping(path = "/authorized/admin/rentalItem/delete/{rentalItemId}")
    //Nie da się usunąć przedmiotu, który jest lub był wypożyczony
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
