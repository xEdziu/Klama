package pwr.isa.klama.rentalItem;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;

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
        return rentalItemService.getRentalItem();
    }

    @GetMapping(path = "/rentalItem/{rentalItemId}")
    public RentalItem getRentalItem(@PathVariable("rentalItemId") Long rentalItemId) {
        return rentalItemService.getRentalItemById(rentalItemId);
    }
    // ============ User methods ============
    @PostMapping(path = "/authorized/rentalItem/rent")
    public Map<String, Object> rentRentalItem(@RequestBody List<RentRequest> rentRequests) {
        return rentalItemService.rentRentalItems(rentRequests);
    }

    // ============ Admin methods ============
    @PostMapping(path = "/authorized/admin/rentalItem/add")
    public Map<String, Object> addNewRentalItem(@RequestBody RentalItem rentalItem) {
        return rentalItemService.addNewRentalItem(rentalItem);
    }

    @DeleteMapping(path = "/authorized/admin/rentalItem/delete/{rentalItemId}")
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
