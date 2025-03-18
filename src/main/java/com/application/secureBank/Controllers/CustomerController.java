package com.application.secureBank.Controllers;

import com.application.secureBank.Services.CustomerService;
import com.application.secureBank.models.Customer;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/customers")
@CrossOrigin(origins = "http://localhost:4200")
@RequiredArgsConstructor
public class CustomerController {
    private final CustomerService customerService;

    public Customer getCustomerProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String customerId = authentication.getName();
        return customerService.findByCustomerId(customerId);
    }
}
