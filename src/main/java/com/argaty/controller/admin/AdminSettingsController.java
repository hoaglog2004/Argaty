package com.argaty.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminSettingsController {

    @GetMapping("/settings")
    public String settings(Model model) {
        model.addAttribute("adminPage", "settings");
        return "admin/settings";
    }

    @PostMapping("/settings/shipping")
    public String updateShippingSettings() {
        return "redirect:/admin/settings";
    }

    @PostMapping("/settings/payment")
    public String updatePaymentSettings() {
        return "redirect:/admin/settings";
    }

    @PostMapping("/settings/email")
    public String updateEmailSettings() {
        return "redirect:/admin/settings";
    }
}
