import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/*
 * This Java source file was generated by the Gradle 'init' task.
 */
public class App {
    private ItemRepository itemRepository;
    private SalesPromotionRepository salesPromotionRepository;

    public App(ItemRepository itemRepository, SalesPromotionRepository salesPromotionRepository) {
        this.itemRepository = itemRepository;
        this.salesPromotionRepository = salesPromotionRepository;
    }

    private String formatDouble(double d) {
        return String.format("%.0f", d);
    }

    public String bestCharge(List<String> inputs) {
        final int INDEX_BUY_30_SAVE_6_YUAN = 0;
        final int INDEX_DISCOUNT_ON_SPECIFIED_ITEMS = 1;

        final List<Item> itemList = itemRepository.findAll();
        final Map<String, Item> itemDict = itemList.stream().collect(Collectors.toMap(Item::getId, item -> item));
        final List<SalesPromotion> promotionList = salesPromotionRepository.findAll();
        final List<String> relatedItems = promotionList.get(INDEX_DISCOUNT_ON_SPECIFIED_ITEMS).getRelatedItems();

        final String MULTIPLIER = " x ";
        final String EQUAL_SIGN = " = ";
        final String NEW_LINE = "\n";
        final String YUAN = " yuan";

        final List<String> itemIds = new ArrayList<>();
        final List<Integer> itemCounts = new ArrayList<>();
        final List<Double> itemPrices = new ArrayList<>();

        inputs.forEach(input -> {
            String[] itemInfo = input.split(MULTIPLIER);
            itemIds.add(itemInfo[0]);
            itemCounts.add(Integer.parseInt(itemInfo[1]));
        });

        double priceTotal = 0;
        double[] promotionTotal = new double[2];
        boolean no_sales_promotion = true;

        for (int i = 0; i < itemIds.size(); i++) {
            String itemId = itemIds.get(i);
            int itemCount = itemCounts.get(i);
            Item item = itemDict.get(itemId);
            final double price = item.getPrice() * itemCount;
            itemPrices.add(price);
            priceTotal += price;
            promotionTotal[INDEX_BUY_30_SAVE_6_YUAN] += price;
            if (relatedItems.contains(itemId)) {
                promotionTotal[INDEX_DISCOUNT_ON_SPECIFIED_ITEMS] += price * 0.5;
            } else {
                promotionTotal[INDEX_DISCOUNT_ON_SPECIFIED_ITEMS] += price;
            }
        }
        if (promotionTotal[INDEX_BUY_30_SAVE_6_YUAN] >= 30) {
            promotionTotal[INDEX_BUY_30_SAVE_6_YUAN] -= 6;
            no_sales_promotion = false;
        }
        if (promotionTotal[INDEX_DISCOUNT_ON_SPECIFIED_ITEMS] < priceTotal) {
            no_sales_promotion = false;
        }

        int MIN_INDEX = INDEX_BUY_30_SAVE_6_YUAN;
        if (promotionTotal[INDEX_DISCOUNT_ON_SPECIFIED_ITEMS] < promotionTotal[MIN_INDEX]) {
            MIN_INDEX = INDEX_DISCOUNT_ON_SPECIFIED_ITEMS;
        }
        StringBuilder res = new StringBuilder("============= Order details =============\n");
        for (int i = 0; i < itemIds.size(); i++) {
            String itemId = itemIds.get(i);
            int itemCount = itemCounts.get(i);
            Item item = itemDict.get(itemId);
            res.append(item.getName()).append(MULTIPLIER).append(itemCount).append(EQUAL_SIGN).append(formatDouble(itemPrices.get(i))).append(YUAN).append(NEW_LINE);
        }
        if (!no_sales_promotion) {
            res.append("-----------------------------------\n");
            res.append("Promotion used:\n");
            if (MIN_INDEX == INDEX_BUY_30_SAVE_6_YUAN) {
                res.append("满30减6 yuan，");
                res.append("saving ").append(formatDouble(priceTotal - promotionTotal[INDEX_BUY_30_SAVE_6_YUAN])).append(YUAN).append(NEW_LINE);
            } else {
                res.append(promotionList.get(INDEX_DISCOUNT_ON_SPECIFIED_ITEMS).getDisplayName());
                res.append(" (");
                String[] relatedItemNames = new String[relatedItems.size()];
                for (int i = 0; i < relatedItemNames.length; i++) {
                    relatedItemNames[i] = itemDict.get(relatedItems.get(i)).getName();
                }
                res.append(String.join("，", relatedItemNames));
                res.append(")，");
                res.append("saving ").append(formatDouble(priceTotal - promotionTotal[INDEX_DISCOUNT_ON_SPECIFIED_ITEMS])).append(YUAN).append(NEW_LINE);
            }
        }

        res.append("-----------------------------------\n");
        res.append("Total：").append(formatDouble(promotionTotal[MIN_INDEX])).append(YUAN).append(NEW_LINE);
        res.append("===================================");
        return res.toString();
    }
}
