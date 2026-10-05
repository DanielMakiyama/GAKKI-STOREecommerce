package com.gakki.store.auditoria;

import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import org.hibernate.Hibernate;
import org.hibernate.proxy.HibernateProxy;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.LinkedHashMap;
import java.util.Map;

// Lê o estado de uma entidade como um mapa campo → valor, para virar o
// JSON do log (RNF0012).
//
// Por que não serializar a entidade direto com Jackson: relação
// bidirecional (Pedido tem itens, item tem pedido) entra em recursão
// infinita, e tocar um campo LAZY dispara consulta no meio do flush. Aqui
// as coleções são puladas e as relações viram só o id.

final class ExtratorDeEstado {

    private ExtratorDeEstado() {
    }

    static Map<String, Object> extrair(Object entidade) {
        Map<String, Object> estado = new LinkedHashMap<>();

        // Sobe a hierarquia: cobre uma eventual @MappedSuperclass.
        for (Class<?> classe = Hibernate.getClass(entidade);
             classe != null && classe != Object.class;
             classe = classe.getSuperclass()) {

            for (Field campo : classe.getDeclaredFields()) {
                if (ignorar(campo)) {
                    continue;
                }
                campo.setAccessible(true);
                try {
                    estado.put(nomeNoLog(campo), valorDe(campo.get(entidade), campo));
                } catch (IllegalAccessException e) {
                    // Um campo ilegível não pode derrubar a operação de
                    // negócio: registra a falha no lugar do valor.
                    estado.put(campo.getName(), "<ilegivel>");
                }
            }
        }
        return estado;
    }

    private static boolean ignorar(Field campo) {
        return campo.isSynthetic()
                || Modifier.isStatic(campo.getModifiers())
                // Campos que a instrumentação do Hibernate acrescenta.
                || campo.getName().startsWith("$$_hibernate")
                // Coleções: fora do log para não carregar o agregado
                // inteiro a cada escrita. Cada item tem log próprio.
                || campo.isAnnotationPresent(OneToMany.class)
                || campo.isAnnotationPresent(ManyToMany.class);
    }

    private static String nomeNoLog(Field campo) {
        return ehRelacao(campo) ? campo.getName() + "Id" : campo.getName();
    }

    private static Object valorDe(Object valor, Field campo) {
        if (valor == null) {
            return null;
        }
        return ehRelacao(campo) ? idDe(valor) : valor;
    }

    private static boolean ehRelacao(Field campo) {
        return campo.isAnnotationPresent(ManyToOne.class)
                || campo.isAnnotationPresent(OneToOne.class);
    }

    // Id de uma entidade relacionada, SEM inicializar o proxy.
    //
    // Chamar getId() no proxy dispararia o SELECT da entidade inteira, no
    // meio do flush. O LazyInitializer já carrega o identificador — é o
    // único dado que o proxy conhece antes de ser inicializado.
    //
    // Quando o campo não é proxy (relação já carregada), sobra ler o campo
    // `id` por reflexão, que não dispara nada.
    private static Object idDe(Object entidadeRelacionada) {
        if (entidadeRelacionada instanceof HibernateProxy proxy) {
            return proxy.getHibernateLazyInitializer().getIdentifier();
        }
        return lerId(entidadeRelacionada);
    }

    private static Object lerId(Object entidade) {
        for (Class<?> classe = Hibernate.getClass(entidade);
             classe != null && classe != Object.class;
             classe = classe.getSuperclass()) {
            try {
                Field id = classe.getDeclaredField("id");
                id.setAccessible(true);
                return id.get(entidade);
            } catch (NoSuchFieldException e) {
                // Continua subindo: o id pode estar na superclasse.
            } catch (IllegalAccessException e) {
                return null;
            }
        }
        return null;
    }
}
