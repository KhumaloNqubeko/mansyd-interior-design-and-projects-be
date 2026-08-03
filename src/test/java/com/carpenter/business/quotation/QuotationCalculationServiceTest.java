package com.carpenter.business.quotation;

import static org.assertj.core.api.Assertions.assertThat;

import com.carpenter.business.customer.Customer;
import com.carpenter.business.servicerequest.ServiceRequest;
import com.carpenter.business.user.AccountStatus;
import com.carpenter.business.user.Role;
import com.carpenter.business.user.User;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class QuotationCalculationServiceTest {
    private final QuotationCalculationService calculator = new QuotationCalculationService();

    @Test
    void recalculatesTotalsWithDiscountAndTax() {
        Quotation quotation = quotation();
        quotation.addItem(new QuotationItem(quotation, QuotationItemType.MATERIAL, "Boards",
                new BigDecimal("2"), new BigDecimal("100.00"), new BigDecimal("10.00"), new BigDecimal("15.00")));
        quotation.addItem(new QuotationItem(quotation, QuotationItemType.LABOUR, "Install",
                new BigDecimal("1"), new BigDecimal("250.00"), new BigDecimal("0.00"), new BigDecimal("0.00")));

        calculator.recalculate(quotation);

        assertThat(quotation.getSubtotal()).isEqualByComparingTo("450.00");
        assertThat(quotation.getDiscountTotal()).isEqualByComparingTo("10.00");
        assertThat(quotation.getTaxTotal()).isEqualByComparingTo("28.50");
        assertThat(quotation.getTotal()).isEqualByComparingTo("468.50");
    }

    private Quotation quotation() {
        User user = new User("customer@example.com", "hash", Role.CUSTOMER, AccountStatus.ACTIVE);
        Customer customer = new Customer(user, "Customer", "+27 00 000 0000", "1 Main", null, "Johannesburg", "2000");
        ServiceRequest request = new ServiceRequest(customer, "Cupboards", "Bedroom", "Email", "1 Main");
        return new Quotation("QUO-TEST", request, LocalDate.now().plusDays(7), "");
    }
}
