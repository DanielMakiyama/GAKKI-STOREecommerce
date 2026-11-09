package com.gakki.store.mapper;

// Normalização e mascaramento de campos formatados.

final class Formatos {

    private Formatos() {
    }

   //Remove tudo que não for dígito. O banco guarda só números
    static String somenteDigitos(String valor) {
        return valor == null ? null : valor.replaceAll("\\D", "");
    }

    //Esconde os blocos externos do CPF: {@code 12345678901} vira {@code ***.456.789-**}.


    static String mascararCpf(String cpf) {
        String digitos = somenteDigitos(cpf);
        if (digitos == null || digitos.length() != 11) {
            return digitos;
        }
        return "***." + digitos.substring(3, 6) + "." + digitos.substring(6, 9) + "-**";
    }
}
