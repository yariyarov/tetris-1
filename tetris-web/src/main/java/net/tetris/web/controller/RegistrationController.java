package net.tetris.web.controller;

import net.tetris.services.NullPlayer;
import net.tetris.services.Player;
import net.tetris.services.PlayerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;

/**
 * User: serhiy.zelenin
 * Date: 5/18/12
 * Time: 6:55 PM
 */
@Controller
@RequestMapping("/register")
public class RegistrationController {
    @Autowired
    private PlayerService playerService;

    public RegistrationController() {
    }

    //for unit test
    RegistrationController(PlayerService playerService) {
        this.playerService = playerService;
    }

    @RequestMapping(method = RequestMethod.GET)
    public String openRegistrationForm(HttpServletRequest request, Model model) {
        String ip = getIp(request);

        Player playerByIp = playerService.findPlayerByIp(ip);
        if (isLocalhost(ip) || playerByIp instanceof NullPlayer) {
            Player player = new Player();
            model.addAttribute("player", player);

            player.setCallbackUrl("http://" + ip + ":8888");

            return "register";
        }
        model.addAttribute("user", playerByIp.getName());
        model.addAttribute("url", playerByIp.getCallbackUrl());

        return "already_registered";
    }

    private boolean isLocalhost(String url) {
        return url.contains("127.0.0.1");
    }

    private String getIp(HttpServletRequest request) {
        String ip = request.getRemoteAddr();
        if (ip.equals("0:0:0:0:0:0:0:1")) {
            ip = "127.0.0.1";
        }
        return ip;
    }

    @RequestMapping(params = "remove_me", method = RequestMethod.GET)
    public String removeUserFromGame(HttpServletRequest request, Model model) {
        String ip = request.getRemoteAddr();
        playerService.removePlayerByIp(ip);
        return "redirect:/";
    }

    @RequestMapping(method = RequestMethod.POST)
    public String submitRegistrationForm(Player player, BindingResult result) {
        if (result.hasErrors()) {
            return "register";
        }
        if (playerService.alreadyRegistered(player.getName())) {
            playerService.updatePlayer(player);
            return "redirect:/board/" + player.getName();
        }
        playerService.addNewPlayer(player.getName(), player.getCallbackUrl(), null);

        if (isLocalhost(player.getCallbackUrl())) {
            return "register";
        }
        return "redirect:/board/" + player.getName();
    }

}
