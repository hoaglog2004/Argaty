package com.argaty.service;

import java.math.BigDecimal;

/**
 * Service tính phí vận chuyển realtime.
 */
public interface ShippingFeeService {

    BigDecimal calculateFee(BigDecimal subtotal,
                            String city,
                            String district,
                            String ward,
                            String address,
                            int itemCount);
}
