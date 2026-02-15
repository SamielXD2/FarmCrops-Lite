package dev.samiel.farmcrops.utils;
import dev.samiel.farmcrops.FarmCrops;
public class MoneyFormatter {
    public static String format(double amount) {
        if (amount < 1000) {
            if (amount == (long) amount) {
                return "$" + (long) amount;
            } else {
                return "$" + String.format("%.2f", amount);
            }
        } else if (amount < 1000000) {
            return "$" + String.format("%.1f", amount / 1000) + "k";
        } else if (amount < 1000000000) {
            return "$" + String.format("%.1f", amount / 1000000) + "M";
        } else {
            return "$" + String.format("%.1f", amount / 1000000000) + "B";
        }
    }
    public static String formatCompact(double amount) {
        return format(amount).replace(".0k", "k")
                             .replace(".0M", "M")
                             .replace(".0B", "B");
    }
    public static String formatDetailed(double amount) {
        return "$" + String.format("%,.2f", amount);
    }
}
