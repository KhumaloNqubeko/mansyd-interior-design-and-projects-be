package com.carpenter.business.quotation;

import com.carpenter.business.exception.UnauthorisedOperationException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import org.springframework.stereotype.Service;

@Service
public class QuotationCalculationService {
    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");

    public void recalculate(Quotation quotation) {
        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal discountTotal = BigDecimal.ZERO;
        BigDecimal taxTotal = BigDecimal.ZERO;
        BigDecimal total = BigDecimal.ZERO;

        for (QuotationItem item : quotation.getItems()) {
            BigDecimal lineSubtotal = money(item.getQuantity().multiply(item.getUnitPrice()));
            if (item.getDiscountAmount().compareTo(lineSubtotal) > 0) {
                throw new UnauthorisedOperationException("Item discount cannot exceed the line subtotal.");
            }
            BigDecimal taxable = lineSubtotal.subtract(item.getDiscountAmount());
            BigDecimal tax = money(taxable.multiply(item.getTaxRate()).divide(ONE_HUNDRED, 6, RoundingMode.HALF_UP));
            BigDecimal lineTotal = money(taxable.add(tax));
            item.updateLineTotal(lineTotal);
            subtotal = subtotal.add(lineSubtotal);
            discountTotal = discountTotal.add(item.getDiscountAmount());
            taxTotal = taxTotal.add(tax);
            total = total.add(lineTotal);
        }

        quotation.updateTotals(money(subtotal), money(discountTotal), money(taxTotal), money(total));
    }

    public BigDecimal money(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }
}
