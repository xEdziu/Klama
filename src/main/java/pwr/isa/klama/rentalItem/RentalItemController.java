package pwr.isa.klama.rentalItem;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "api/v1/rentalItem")
public class RentalItemController {

    private final RentalItemService rentalItemService;

    @Autowired
    public RentalItemController(RentalItemService rentalItemService) {
        this.rentalItemService = rentalItemService;
    }

    @GetMapping
    public List<RentalItem> getRentalItems() {
        return rentalItemService.getRentalItem();
    }

    @GetMapping(path = "{rentalItemId}")
    public RentalItem getRentalItem(@PathVariable("rentalItemId") Long rentalItemId) {
        return rentalItemService.getRentalItemById(rentalItemId);
    }

    @PostMapping
    public void addNewRentalItem(@RequestBody RentalItem rentalItem) {
        rentalItemService.addNewRentalItem(rentalItem);
    }

    @DeleteMapping(path = "{rentalItemId}")
    public void deleteRentalItem(@PathVariable("rentalItemId") Long rentalItemID) {
        rentalItemService.deleteRentalItem(rentalItemID);
    }

    @PutMapping(path = "{rentalItemId}")
    public void updateRentalItem(
            @PathVariable("rentalItemId") Long rentalItemId,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) Float price,
            @RequestParam(required = false) Integer quantity) {
         rentalItemService.updateRentalItem(rentalItemId, name, description, price, quantity);
    }
}
