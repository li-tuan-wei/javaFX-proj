package util;

import java.util.Random;
import java.util.Stack;

/**
 * Created by ldh on 2019/1/17.
 */
public class BankCardNoUtil {

    public String createICBCBankCardNo() {
        return credit_card_number(new String[]{"621226"}, 19, 1)[0];
    }

    public String createICBCBankCardNo(boolean isEvent) {
        String cardNumber = "";
        boolean isNext = false;
        do {
            cardNumber = credit_card_number(new String[]{"621226"}, 19, 1)[0];
            if ((Integer.parseInt(cardNumber.substring(18)) % 2 == 0 && isEvent) || (Integer.parseInt(cardNumber.substring(18)) % 2 != 0 && !isEvent)) {
                return cardNumber;
            } else {
                isNext = true;
            }
        } while (isNext);
        return cardNumber;
    }

    public String[] credit_card_number(String[] prefixList, int length, int howMany) {
        Stack<String> result = new Stack<String>();
        for (int i = 0; i < howMany; i++) {
            int randomArrayIndex = (int) Math.floor(Math.random() * prefixList.length);
            String ccnumber = prefixList[randomArrayIndex];
            result.push(completed_number(ccnumber, length));
        }
        return result.toArray(new String[result.size()]);
    }

    public String completed_number(String string, int length) {
        int l = length - string.length();
        String result = string;
        Random r = new Random();
        for (int i=0; i<l; i++) {
            int randomArrayIndex = r.nextInt(10);
            result += randomArrayIndex+"";
        }
        return result;
    }

    public static void main(String[] args) {
        BankCardNoUtil bankCardNoUtil = new BankCardNoUtil();
        for (int i=0; i<100; i++) {
            String code = bankCardNoUtil.createICBCBankCardNo();
            System.out.println("code:" + code + "," + code.length());
        }
    }
}
