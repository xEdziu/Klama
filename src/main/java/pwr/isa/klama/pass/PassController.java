package pwr.isa.klama.pass;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import pwr.isa.klama.pass.userPass.UserPassDTO;
import pwr.isa.klama.pass.userPass.UserPassHistoryDTO;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(path = "api/v1")
public class PassController {

    private final PassService passService;

    @Autowired
    public PassController(PassService passService) {
        this.passService = passService;
    }

    // ============ Freely available methods ============
    @GetMapping(path = "/pass/all")
    public List<PassDTO> getPasses() {
        return passService.getPasses();
    }

    @GetMapping(path = "/pass/{passId}")
    public Pass getPassById(@PathVariable("passId") Long passId) {
        return passService.getPassById(passId);
    }

    // ============ User methods ============
    @GetMapping(path = "/authorized/pass/passHistory")
    public List<UserPassHistoryDTO> getPassHistory() {
        return passService.getPassHistory();
    }

    @PostMapping(path = "/authorized/pass/buyNew/{passId}")
    public Map<String, Object> buyNewPass(@PathVariable("passId") Long passId) {
        return passService.buyNewPass(passId);
    }

    // ============ Admin methods ============
    @GetMapping(path = "/authorized/admin/pass/passHistory/all")
    public List<UserPassDTO> getPassHistoryAll() {
        return passService.getPassHistoryAll();
    }

    @GetMapping(path = "/authorized/admin/pass/all")
    public List<Pass> getPassesForAdmin() {
        return passService.getPassesForAdmin();
    }

    @PostMapping(path = "/authorized/admin/pass/add")
    public Map<String, Object> addPass(@RequestBody Pass pass) {
        return passService.addPass(pass);
    }

    @DeleteMapping(path = "/authorized/admin/pass/delete/{passId}")
    public Map<String, Object> deletePass(@PathVariable("passId") Long passId) {
        return passService.deletePass(passId);
    }

    @PutMapping(path = "/authorized/admin/pass/disable/{passId}")
    public Map<String, Object> disablePass(@PathVariable("passId") Long passId) {
        return passService.disablePass(passId);
    }

    @PutMapping(path = "/authorized/admin/pass/update/{passId}")
    public Map<String, Object> updatePass(@RequestBody Pass pass,
                                          @PathVariable("passId") Long passId) {
        return passService.updatePass(pass, passId);
    }

}
