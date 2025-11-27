package com.lotosia.shoppingservice.dto.order;

/**
 * @author: nijataghayev
 */

import com.lotosia.shoppingservice.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatusUpdate {
    private OrderStatus status;
}
