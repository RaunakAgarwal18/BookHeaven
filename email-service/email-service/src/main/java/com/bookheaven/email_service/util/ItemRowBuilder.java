package com.bookheaven.email_service.util;

import com.bookheaven.email_service.dto.event.OrderDeliveredEvent;
import com.bookheaven.email_service.dto.event.OrderShippedEvent;
import com.bookheaven.email_service.dto.event.SellerOrderEvent;

import java.util.List;

public class ItemRowBuilder {

    public static String buildSellerItemsHtml(List<SellerOrderEvent.OrderedBookItem> items, String currency, double[] totalHolder) {
        StringBuilder itemsHtml = new StringBuilder();
        double total = 0;
        if (items != null) {
            for (SellerOrderEvent.OrderedBookItem item : items) {
                double lineTotal = item.getPrice() * item.getQuantity();
                total += lineTotal;
                itemsHtml.append(
                        "<div style=\"margin-bottom:8px; padding-bottom:8px; border-bottom:1px dashed #dde;\">")
                        .append("<div><strong>Book:</strong> ").append(item.getBookTitle()).append("</div>")
                        .append("<div><strong>Qty:</strong> ").append(item.getQuantity())
                        .append(" &nbsp;|&nbsp; <strong>Subtotal:</strong> ").append(currency)
                        .append(" ").append(String.format("%.2f", lineTotal)).append("</div>")
                        .append("</div>");
            }
        }
        totalHolder[0] = total;
        return itemsHtml.toString();
    }

    public static String buildShippedItemsHtml(List<OrderShippedEvent.ShippedItem> items) {
        StringBuilder itemsHtml = new StringBuilder();
        if (items != null) {
            for (OrderShippedEvent.ShippedItem item : items) {
                itemsHtml.append("<div style=\"margin-bottom:6px; padding-bottom:6px; border-bottom:1px dashed #dde;\">")
                        .append("<strong>").append(item.getBookTitle()).append("</strong>")
                        .append(" &nbsp;|&nbsp; Qty: ").append(item.getQuantity())
                        .append("</div>");
            }
        }
        return itemsHtml.toString();
    }

    public static String buildDeliveredItemsHtml(List<OrderDeliveredEvent.DeliveredItem> items) {
        StringBuilder itemsHtml = new StringBuilder();
        if (items != null) {
            for (OrderDeliveredEvent.DeliveredItem item : items) {
                itemsHtml.append("<div style=\"margin-bottom:6px; padding-bottom:6px; border-bottom:1px dashed #dde;\">")
                        .append("<strong>").append(item.getBookTitle()).append("</strong>")
                        .append(" &nbsp;|&nbsp; Qty: ").append(item.getQuantity())
                        .append("</div>");
            }
        }
        return itemsHtml.toString();
    }
}
