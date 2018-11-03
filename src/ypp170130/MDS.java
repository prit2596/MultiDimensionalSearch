// Change to your net id
package ypp170130;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;

/** Starter code for LP3
 *  @author
 */

// If you want to create additional classes, place them in this file as subclasses of MDS

public class MDS {
    // Add fields of MDS here

    private TreeSet<Long> sortedKeys;
    private HashMap<Long, Item> treeMap;
    private HashMap<Money, HashSet<Long>> moneyKeyMap;
    private HashMap<Long, TreeSet<Item>> descriptionKeyMap;
    private Money ZERO;

    private class Item implements Comparable<Item>{

        private Long id;
        private Money price;
        private HashSet<Long> description;

        Item(Long id, Money price, List<Long> description){
            this.id = id;
            this.price = price;
            this.description = new HashSet<>(description);
        }

        public Long getId() {
            return id;
        }

        public Money getPrice() {
            return price;
        }

        public HashSet<Long> getDescription() {
            return description;
        }

        @Override
        public int compareTo(Item item) {
            int result = this.getPrice().compareTo(item.getPrice());
            if(result!=0)
                return result;
            else{
                return this.getId().compareTo(item.getId());
            }
        }
    }

    // Constructors
    public MDS() {
        treeMap = new HashMap<>();
        sortedKeys = new TreeSet<>();
        moneyKeyMap = new HashMap<>();
        descriptionKeyMap = new HashMap<>();
        ZERO = new Money();
    }

    /* Public methods of MDS. Do not change their signatures.
       __________________________________________________________________
       a. Insert(id,price,list): insert a new item whose description is given
       in the list.  If an entry with the same id already exists, then its
       description and price are replaced by the new values, unless list
       is null or empty, in which case, just the price is updated.
       Returns 1 if the item is new, and 0 otherwise.
    */

    public int insert(long id, Money price, List<Long> list) {

        if(treeMap.containsKey(id)){

            if(list == null || list.size() == 0){
                list = new ArrayList<>(treeMap.get(id).getDescription());
            }
            delete(id);
            insert(id, price, list);
            return 0;
        }

        Item item = new Item(id, price, list);
        sortedKeys.add(id);
        treeMap.put(id, item);
        HashSet<Long> moneyKeySet;
        TreeSet<Item> descriptionKeySet;
        moneyKeySet = moneyKeyMap.get(price);

        if(moneyKeySet == null){
            moneyKeyMap.put(price, new HashSet<Long>(){{add(id);}});
        }
        else{
            moneyKeySet.add(id);
        }

        for(Long d: list){
            descriptionKeySet = descriptionKeyMap.get(d);

            if(descriptionKeySet == null){
                descriptionKeyMap.put(d, new TreeSet<Item>(){{add(item);}});
            }
            else{
                descriptionKeySet.add(item);
            }
        }

        return 1;
    }

    // b. Find(id): return price of item with given id (or 0, if not found).
    public Money find(long id) {
        if(treeMap.containsKey(id)){
            return treeMap.get(id).getPrice();
        }
        return ZERO;
    }

    /*
       c. Delete(id): delete item from storage.  Returns the sum of the
       long ints that are in the description of the item deleted,
       or 0, if such an id did not exist.
    */
    public long delete(long id) {
        sortedKeys.remove(id);
        Item item = treeMap.remove(id);
        if(item != null){
            long sum = 0;
            Money price = item.getPrice();
            HashSet<Long> moneyKeySet = moneyKeyMap.get(price);

            if(moneyKeySet.size() > 1){
                moneyKeySet.remove(id);
            }
            else{
                moneyKeyMap.remove(price);
            }

            TreeSet<Item> descriptionKeySet;
            for(Long d: item.getDescription()){
                sum += d;
                descriptionKeySet = descriptionKeyMap.get(d);

                if(descriptionKeySet.size() > 1){
                    descriptionKeySet.remove(item);
                }
                else{
                    descriptionKeyMap.remove(d);
                }
            }
            return sum;
        }
        return 0;
    }

    /*
       d. FindMinPrice(n): given a long int, find items whose description
       contains that number (exact match with one of the long ints in the
       item's description), and return lowest price of those items.
       Return 0 if there is no such item.
    */
    public Money findMinPrice(long n) {
        TreeSet<Item> items = descriptionKeyMap.get(n);

        if(items!=null){
            return items.first().getPrice();
        }
        return ZERO;
    }

    /*
       e. FindMaxPrice(n): given a long int, find items whose description
       contains that number, and return highest price of those items.
       Return 0 if there is no such item.
    */
    public Money findMaxPrice(long n) {
        TreeSet<Item> items = descriptionKeyMap.get(n);

        if(items!=null){
            return items.last().getPrice();
        }
        return ZERO;
    }

    /*
       f. FindPriceRange(n,low,high): given a long int n, find the number
       of items whose description contains n, and in addition,
       their prices fall within the given range, [low, high].
    */
    public int findPriceRange(long n, Money low, Money high) {
        if(high.compareTo(low) == -1){
            return 0;
        }

        Set<Item> itemSet = descriptionKeyMap.get(n);

        if(itemSet == null){
            return 0;
        }

        int count = 0;

        for(Item item: itemSet){
            if(low.compareTo(item.getPrice())!= 1 && item.getPrice().compareTo(high) != 1){
                count++;
            }
        }

        return count;


//        Set<Money> moneySet = moneyKeyMap.subMap(low, true, high, true).keySet().stream().map(Money::new).collect(Collectors.toSet());
//        int count = 0;
//        for(Money price: moneySet){
//            Set<Long> features = moneyKeyMap.get(price);
//            if(features != null && features.contains(n)){
//                count++;
//            }
//        }
//        return count;
    }

    /*
       g. PriceHike(l,h,r): increase the price of every product, whose id is
       in the range [l,h] by r%.  Discard any fractional pennies in the new
       prices of items.  Returns the sum of the net increases of the prices.
    */
    public Money priceHike(long l, long h, double rate) {
        if(l<=h){
            BigDecimal sum = new BigDecimal("0");
            //Set<Long> keys = treeMap.subMap(l, true, h, true).keySet().stream().map(Long::new).collect(Collectors.toSet());
            Set<Long> keys = sortedKeys.subSet(l, true, h, true).stream().map(Long::new).collect(Collectors.toSet());
            for(Long key: keys){
                Item item = treeMap.get(key);
                delete(key);
                BigDecimal oldPrice = new BigDecimal(item.getPrice().toString());
                BigDecimal rateObj = new BigDecimal(""+rate);
                BigDecimal hike = new BigDecimal(truncateFractionalPennies(oldPrice.multiply(rateObj.divide(new BigDecimal(100)))));
                Money newPrice = parseMoney(truncateFractionalPennies(oldPrice.add(hike)));
                sum = sum.add(hike);
//                sum+=oldPrice*(rate/100);
                insert(key, newPrice, new LinkedList<>(item.getDescription()));
            }
            return parseMoney(truncateFractionalPennies(sum));
        }
        return ZERO;
    }

    private String truncateFractionalPennies(BigDecimal d){
        DecimalFormat decimalFormat = new DecimalFormat("#0.00");
        decimalFormat.setRoundingMode(RoundingMode.DOWN);
        return decimalFormat.format(d);
    }

    private Money parseMoney(String s){
        String[] arr = s.split("\\.");
        return new Money(Long.parseLong(arr[0]), Integer.parseInt(arr[1]));
    }
    /*
      h. RemoveNames(id, list): Remove elements of list from the description of id.
      It is possible that some of the items in the list are not in the
      id's description.  Return the sum of the numbers that are actually
      deleted from the description of id.  Return 0 if there is no such id.
    */
    public long removeNames(long id, List<Long> list) {
        Item item = treeMap.get(id);
        if(item != null){
            int count = 0;
            delete(id);
            for(long name: list){
                if(item.getDescription().contains(name)){
                    item.getDescription().remove(name);
                    count+=name;
                }
            }
            //add
            insert(id, item.getPrice(), new ArrayList<>(item.getDescription()));
            return count;
        }
        return 0;
    }

    // Do not modify the Money class in a way that breaks LP3Driver.java
    public static class Money implements Comparable<Money> {
        long d;  int c;
        public Money() { d = 0; c = 0; }
        public Money(long d, int c) { this.d = d; this.c = c; }
        public Money(String s) {
            String[] part = s.split("\\.");
            int len = part.length;
            if(len < 1) { d = 0; c = 0; }
            else if(part.length == 1) { d = Long.parseLong(s);  c = 0; }
            else { d = Long.parseLong(part[0]);  c = Integer.parseInt(part[1]); }
        }

        public Money(Money money) {
        }

        public long dollars() { return d; }
        public int cents() { return c; }
        public int compareTo(Money other) { // Complete this, if needed

            if(this.dollars() < other.dollars()){
                return -1;
            }
            else if(this.dollars() == other.dollars()){
                if(this.cents() < other.cents()){
                    return -1;
                }
                else if(this.cents() == other.cents()){
                    return 0;
                }
                else{
                    return 1;
                }
            }
            else{
                return 1;
            }
        }

        public String toString() {
            return d + "." + String.format("%2s",c).replace(' ','0');
        }
    }

}