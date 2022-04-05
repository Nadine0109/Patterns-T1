package ru.netology.delivery.test;

import org.junit.jupiter.api.*;
import org.openqa.selenium.Keys;
import ru.netology.delivery.data.GenerateData;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selenide.*;

import com.codeborne.selenide.logevents.SelenideLogger;
import io.qameta.allure.selenide.AllureSelenide;
//import io.qameta.allure.selenide.AllureSelenide;


class DeliveryTest {

    @BeforeAll
    static void allureSetup() {
        SelenideLogger.addListener("AllureSelenide", new AllureSelenide());
    }

    @AfterAll
    static void tearDownAll() {
        SelenideLogger.removeListener("allure");
    }

    @BeforeEach
    void setup() {
        open("http://localhost:7777");
        $("[data-test-id='date']").$("[class='input__control']").click();
        $("[data-test-id='date']").$("[class='input__control']").
                sendKeys(Keys.chord(Keys.CONTROL + "A", Keys.DELETE));
    }

    @AfterEach
    void tearDown() {
        close();
    }

    @Test
    @DisplayName("Should successfully reschedule the meeting")
    void shouldSuccessfullyRescheduleMeeting() {
        var validUser = GenerateData.Registration.generateUser("ru");
        var daysToAddForFirstMeeting = GenerateData.generateRandomDateShift();
        var firstMeetingDate = GenerateData.generateDate(daysToAddForFirstMeeting);
        var daysToAddForSecondMeeting = GenerateData.generateRandomDateShift();
        var secondMeetingDate = GenerateData.generateDate(daysToAddForSecondMeeting);
        $("[data-test-id='city']").$("[placeholder='Город']").setValue(validUser.getCity());
        $("[data-test-id='date']").$("[placeholder='Дата встречи']").setValue(firstMeetingDate);
        $("[data-test-id= 'name']").$("[name ='name']").setValue(validUser.getName());
        $("[data-test-id='phone']").$("[name='phone']").setValue(validUser.getPhone());
        $("[data-test-id='agreement']").click();
        $$("button").find(exactText("Запланировать")).click();
        $("[data-test-id='success-notification']").shouldBe(visible).
                shouldHave(exactText("Успешно!\n" + "Встреча успешно запланирована на " + firstMeetingDate));
        setup();
        $("[data-test-id='city']").$("[placeholder='Город']").setValue(validUser.getCity());
        $("[data-test-id='date']").$("[placeholder='Дата встречи']").setValue(secondMeetingDate);
        $("[data-test-id= 'name']").$("[name ='name']").setValue(validUser.getName());
        $("[data-test-id='phone']").$("[name='phone']").setValue(validUser.getPhone());
        $("[data-test-id='agreement']").click();
        $$("button").find(exactText("Запланировать")).click();
        $("[data-test-id= 'replan-notification']").shouldHave(exactText("Необходимо подтверждение\n" +
                "У вас уже запланирована встреча на другую дату. Перепланировать?\n" + "Перепланировать"));
        $$("button").find(exactText("Перепланировать")).shouldBe(visible).click();
        $("[data-test-id='success-notification']").shouldBe(visible).
                shouldHave(exactText("Успешно!\n" + "Встреча успешно запланирована на " + secondMeetingDate));
    }

    @Test
    void shouldWarnIfTheCityNotAvailable() {
        $("[data-test-id='city']").$("[placeholder='Город']").setValue(GenerateData.generateInvalidCity());
        $("[data-test-id='date']").$("[placeholder='Дата встречи']").setValue(GenerateData.
                generateDate(GenerateData.generateRandomDateShift()));
        $("[data-test-id= 'name']").$("[name ='name']").setValue(GenerateData.generateName("ru"));
        $("[data-test-id='phone']").$("[name='phone']").setValue(GenerateData.generatePhone());
        $("[data-test-id='agreement']").click();
        $$("button").find(exactText("Запланировать")).click();
        $("[data-test-id='city'].input_invalid .input__sub").shouldBe(visible).
                shouldHave(exactText("Доставка в выбранный город недоступна"));
    }

    @Test
    void shouldWarnTheDateNotAvailable() {
        $("[data-test-id='city']").$("[placeholder='Город']").setValue(GenerateData.generateValidCity());
        $("[data-test-id='date']").$("[class='input__control']").setValue(GenerateData.
                generateDate(-1));
        $("[data-test-id= 'name']").$("[name ='name']").setValue(GenerateData.generateName("ru"));
        $("[data-test-id='phone']").$("[name='phone']").setValue(GenerateData.generatePhone());
        $("[data-test-id='agreement']").click();
        $$("button").find(exactText("Запланировать")).click();
        $("[data-test-id='date'] .input__sub").shouldBe(visible).
                shouldHave(exactText("Заказ на выбранную дату невозможен"));
    }

    @Test
    void shouldInformAboutSuccessfulRescheduling() {
        $("[data-test-id='city']").$("[placeholder='Город']").setValue(GenerateData.generateValidCity());
        $("[data-test-id='date']").$("[placeholder='Дата встречи']").setValue(GenerateData.
                generateDate(4));
        $("[data-test-id= 'name']").$("[name ='name']").setValue(GenerateData.generateName("ru"));
        $("[data-test-id='phone']").$("[name='phone']").setValue(GenerateData.generatePhone());
        $("[data-test-id='agreement']").click();
        $$("button").find(exactText("Запланировать")).click();
        $("[data-test-id='success-notification']").shouldBe(visible).
                shouldHave(exactText("Успешно!\n" + "Встреча успешно запланирована  на " +
                        GenerateData.generateDate(4)));
    }

    @Test
    void shouldWarnOfInvalidDate() {
        $("[data-test-id='city']").$("[placeholder='Город']").setValue(GenerateData.generateValidCity());
        $("[data-test-id='date']").$("[placeholder='Дата встречи']").setValue(GenerateData.
                generateInvalidDate(67));
        $("[data-test-id= 'name']").$("[name ='name']").setValue(GenerateData.generateName("ru"));
        $("[data-test-id='phone']").$("[name='phone']").setValue(GenerateData.generatePhone());
        $("[data-test-id='agreement']").click();
        $$("button").find(exactText("Запланировать")).click();
        $("[data-test-id='date'] .input__sub").shouldBe(visible).
                shouldHave(exactText("Заказ на выбранную дату невозможен"));
    }

    @Test
    void shouldTestTheSameDateAsToday() {
        $("[data-test-id='city']").$("[placeholder='Город']").setValue(GenerateData.generateValidCity());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        $("[data-test-id='date']").$("[placeholder='Дата встречи']").setValue(LocalDate.now().format(formatter));
        $("[data-test-id= 'name']").$("[name ='name']").setValue(GenerateData.generateName("ru"));
        $("[data-test-id='phone']").$("[name='phone']").setValue(GenerateData.generatePhone());
        $("[data-test-id='agreement']").click();
        $$("button").find(exactText("Запланировать")).click();
        $("[data-test-id='date'] .input__sub").shouldBe(visible).
                shouldHave(exactText("Заказ на выбранную дату невозможен"));
    }

    @Test
    void shouldWarnOfInvalidDateFormat() {
        $("[data-test-id='city']").$("[placeholder='Город']").setValue(GenerateData.generateValidCity());
        $("[data-test-id='date']").$("[placeholder='Дата встречи']").setValue("80.14.9484");
        $("[data-test-id= 'name']").$("[name ='name']").setValue(GenerateData.generateName("ru"));
        $("[data-test-id='phone']").$("[name='phone']").setValue(GenerateData.generatePhone());
        $("[data-test-id='agreement']").click();
        $$("button").find(exactText("Запланировать")).click();
        $("[data-test-id='date'] .input__sub").shouldBe(visible).
                shouldHave(exactText("Неверно введена дата"));
    }

    @Test
    void shouldWarnAboutInvalidNameSymbols() {
        $("[data-test-id='city']").$("[placeholder='Город']").setValue(GenerateData.generateValidCity());
        $("[data-test-id='date']").$("[placeholder='Дата встречи']").setValue(GenerateData.
                generateDate(GenerateData.generateRandomDateShift()));
        $("[data-test-id= 'name']").$("[name ='name']").setValue("John Snow");
        $("[data-test-id='phone']").$("[name='phone']").setValue(GenerateData.generatePhone());
        $("[data-test-id='agreement']").click();
        $$("button").find(exactText("Запланировать")).click();
        $("[data-test-id='name'].input_invalid .input__sub").shouldBe(visible)
                .shouldHave(exactText("Имя и Фамилия указаные неверно. Допустимы только русские буквы, пробелы и дефисы."));
    }

    @Test
    void shouldRequireAgreementCheckbox() {
        $("[data-test-id='city']").$("[placeholder='Город']").setValue(GenerateData.generateValidCity());
        $("[data-test-id='date']").$("[placeholder='Дата встречи']").setValue(GenerateData.
                generateDate(GenerateData.generateRandomDateShift()));
        $("[data-test-id= 'name']").$("[name ='name']").setValue(GenerateData.generateName("ru"));
        $("[data-test-id='phone']").$("[name='phone']").setValue(GenerateData.generatePhone());
        $$("button").find(exactText("Запланировать")).click();
        $("[data-test-id='agreement'].input_invalid .checkbox__text").shouldBe(visible)
                .shouldHave(exactText("Я соглашаюсь с условиями обработки и использования моих персональных данных"));
    }

    @Test
    void shouldAddPlusToPhone() {
        String validPhone = GenerateData.generatePhone();
        String withoutPlusPhone = validPhone.substring(1, 12);
        validPhone = validPhone.replaceAll("\\s", "");
        $("[data-test-id='phone']").$("[name='phone']").setValue(withoutPlusPhone);
        $("[data-test-id='phone']").$("[name='phone']").
                shouldHave(value(GenerateData.formatPhone(validPhone)));
    }

    @Test
    void shouldLimitPhoneNumberToElevenNumbers() {
        String validPhone = GenerateData.generatePhone();
        String invalidPhone = validPhone + "7899";
        $("[data-test-id='phone']").$("[name='phone']").setValue(invalidPhone);
        $("[data-test-id='phone']").$("[class= 'input__control']").
                shouldHave(value(GenerateData.formatPhone(validPhone)));
    }

    @Test
    void shouldWarnAboutInvalidPhoneFormat() {
        $("[data-test-id='city']").$("[placeholder='Город']").setValue(GenerateData.generateValidCity());
        $("[data-test-id='date']").$("[placeholder='Дата встречи']").setValue(GenerateData.
                generateDate(GenerateData.generateRandomDateShift()));
        $("[data-test-id= 'name']").$("[name ='name']").setValue(GenerateData.generateName("ru"));
        String validPhone = GenerateData.generatePhone();
        String invalidPhone = validPhone.substring(0, 11);
        $("[data-test-id='phone']").$("[name='phone']").setValue(invalidPhone);
        $("[data-test-id='agreement']").click();
        $$("button").find(exactText("Запланировать")).click();
        $("[data-test-id='phone'].input_invalid .input__sub").shouldBe(visible)
                .shouldHave(exactText("Телефон указан неверно. Должно быть 11 цифр, например, +79012345678."));
    }

    @Test
    void shouldRequireValidCityIfEmpty() {
        $("[data-test-id='city']").$("[placeholder='Город']").setValue("");
        $("[data-test-id='date']").$("[placeholder='Дата встречи']").setValue(GenerateData.
                generateDate(GenerateData.generateRandomDateShift()));
        $("[data-test-id= 'name']").$("[name ='name']").setValue(GenerateData.generateName("ru"));
        $("[data-test-id='phone']").$("[name='phone']").setValue(GenerateData.generatePhone());
        $("[data-test-id='agreement']").click();
        $$("button").find(exactText("Запланировать")).click();
        $("[data-test-id='city'].input_invalid .input__sub").shouldBe(visible).
                shouldHave(exactText("Поле обязательно для заполнения"));
    }

    @Test
    void shouldRequireValidDateIfEmpty() {
        $("[data-test-id='city']").$("[placeholder='Город']").setValue(GenerateData.generateValidCity());
        $("[data-test-id= 'name']").$("[name ='name']").setValue(GenerateData.generateName("ru"));
        $("[data-test-id='phone']").$("[name='phone']").setValue(GenerateData.generatePhone());
        $("[data-test-id='agreement']").click();
        $$("button").find(exactText("Запланировать")).click();
        $("[data-test-id='date'] .input__sub").shouldBe(visible).
                shouldHave(exactText("Неверно введена дата"));
    }

    @Test
    void shouldRequireValidNameIfEmpty() {
        $("[data-test-id='city']").$("[placeholder='Город']").setValue(GenerateData.generateValidCity());
        $("[data-test-id='date']").$("[placeholder='Дата встречи']").setValue(GenerateData.
                generateDate(GenerateData.generateRandomDateShift()));
        $("[data-test-id= 'name']").$("[name ='name']").setValue("");
        $("[data-test-id='phone']").$("[name='phone']").setValue(GenerateData.generatePhone());
        $("[data-test-id='agreement']").click();
        $$("button").find(exactText("Запланировать")).click();
        $("[data-test-id='name'].input_invalid .input__sub").shouldBe(visible)
                .shouldHave(exactText("Поле обязательно для заполнения"));
    }

    @Test
    void shouldRequireValidPhoneNumberIfEmpty() {
        $("[data-test-id='city']").$("[placeholder='Город']").setValue(GenerateData.generateValidCity());
        $("[data-test-id='date']").$("[placeholder='Дата встречи']").setValue(GenerateData.
                generateDate(GenerateData.generateRandomDateShift()));
        $("[data-test-id= 'name']").$("[name ='name']").setValue(GenerateData.generateName("ru"));
        $("[data-test-id='phone']").$("[name='phone']").setValue("");
        $("[data-test-id='agreement']").click();
        $$("button").find(exactText("Запланировать")).click();
        $("[data-test-id='phone'].input_invalid .input__sub").shouldBe(visible)
                .shouldHave(exactText("Поле обязательно для заполнения"));
    }
}