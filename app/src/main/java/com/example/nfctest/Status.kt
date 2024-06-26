package com.example.nfctest

enum class Status(val code: Int) {
    NOTSUPPORTED(0) {override fun toString(): String {return "NFC не поддерживается на данном устройстве."}    },
    SCAN(10) {override fun toString(): String {return "Просканируйте NFC-метку."}},
    ;

    override fun toString(): String {return "ОШИБКА: Неизвестный статус."}
}

enum class Notification(val code: Int) {
    SCANFAILURE(15) {override fun toString(): String {return "Во время сканирования метки произошла ошибка, попробуйте снова."}},
    HTTPFAILURE(20) {override fun toString(): String {return "Ошибка при передаче данных на сервер. Попробуйте снова."}},
    SUCСESS(30) {override fun toString(): String {return "Сканирование успешно. Информация была передана на сервер."}}
    ;

    override fun toString(): String {return "ОШИБКА: Неизвестное сообщение."}
}