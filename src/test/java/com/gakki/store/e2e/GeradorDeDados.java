package com.gakki.store.e2e;

import java.util.concurrent.ThreadLocalRandom;

public final class GeradorDeDados {

    private GeradorDeDados() {
    }

    public static String cpfValido() {
        int[] d = new int[11];
        for (int i = 0; i < 9; i++) {
            d[i] = ThreadLocalRandom.current().nextInt(10);
        }
        d[9] = digitoVerificador(d, 9);
        d[10] = digitoVerificador(d, 10);

        StringBuilder cpf = new StringBuilder();
        for (int digito : d) {
            cpf.append(digito);
        }
        return cpf.toString();
    }

    //CPF com formato correto e dígitos verificadores errados
    public static String cpfInvalido() {
        return "11111111111";
    }

    public static String emailUnico() {
        return "e2e-" + System.currentTimeMillis() + "-"
                + ThreadLocalRandom.current().nextInt(1000) + "@example.com";
    }

    public static String senhaForte() {
        return "Gakki@2026";
    }

    // Oito caracteres, sem maiúscula e sem símbolo — viola a RNF0031.
    public static String senhaFraca() {
        return "gakki2026";
    }

    private static int digitoVerificador(int[] digitos, int ate) {
        int soma = 0;
        int peso = ate + 1;
        for (int i = 0; i < ate; i++) {
            soma += digitos[i] * peso--;
        }
        int resto = (soma * 10) % 11;
        return resto == 10 ? 0 : resto;
    }
}
