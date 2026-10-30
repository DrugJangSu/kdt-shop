package regex;

public class RegexIntro {

    public static void main(String[] args) {
//        String value = "주소06236";
//        System.out.println("equals= " + value.equals("06236"));
//        System.out.println("contain= " + value.contains("06236"));
//        System.out.println("matches=" + value.matches("[0-9]{5}"));
//
//        String[] inputs = {"06236", "12345", "6236", "0623A", "06236-"};
//        for (String input : inputs) {
//            System.out.println(input + " -> " + input.matches("[0-9]{5}"));
//        }

//        [abc]
//        [a-z]
//        [A-Z]
//        [a-zA-Z]
//        [a-zA-Z0-9_]
//        [가-힣]
//        [^0-9]
//        [0-9]?
//        [0-9]*
//        [0-9]+
//        [0-9]{3}
//        [0-9]{2,4}
//        [0-9]{2,}

        // 1. 전체 길이는 4글자 이상 12글자 이하입니다.
        // 2. 첫 글자는 영문 소문자입니다.
        // 3, 두 번째 글자부터는 영문 소문자, 숫자, 밑줄만 허용됩니다.

//        [a-z][a-z0-9_]{3,11}


//
//        String regex = "\\d{3}";
//        System.out.println("rule=" + regex);
//        System.out.println("short=" + "123".matches(regex));
//        System.out.println("range=" + "123".matches("[0-9]{3}"));
//        System.out.println("wrong=" + "12a".matches(regex));

        String phone = "010-1234-5678";
        String regex = "010-[0-9]{4}-[0-9]{4}";
        if (phone.matches(regex)) {
            String normalized = phone.replace("-", "");
            System.out.println("저장할 값=" + normalized);
        } else {
            System.out.println("거절");
        }
        System.out.println("원본" + phone);
    }
}
