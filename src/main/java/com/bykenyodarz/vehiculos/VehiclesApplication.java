package com.bykenyodarz.vehiculos;

import com.bykenyodarz.vehiculos.models.Vehiculo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

@SpringBootApplication
@Slf4j
public class VehiclesApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(VehiclesApplication.class, args);
    }

    public static String getStringFromFileName() throws IOException {
        return StreamUtils
                .copyToString(new ClassPathResource("static/vehiculos.json")
                        .getInputStream(), StandardCharsets.UTF_8);
    }

    private static String setPlaca() {
        var letras = Arrays.asList("A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M",
                "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z");
        var numbers = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 0);
        return String.format("%s%s%s - %s%s%s",
                letras.get(new Random().nextInt(letras.size())),
                letras.get(new Random().nextInt(letras.size())),
                letras.get(new Random().nextInt(letras.size())),
                numbers.get(new Random().nextInt(numbers.size())),
                numbers.get(new Random().nextInt(numbers.size())),
                numbers.get(new Random().nextInt(numbers.size())));
    }

    private static List<Vehiculo> getVehicles(String json) throws JsonProcessingException {
        final ObjectMapper objectMapper = new ObjectMapper();
        Vehiculo[] array = objectMapper.readValue(json, Vehiculo[].class);
        return Arrays.asList(array);
    }

    private static Map<Double, List<Vehiculo>> getVehicleByValue(List<Vehiculo> list) {
        LocalDate getLocalDate = LocalDate.now();
        return list.stream().peek(vehiculo -> {
            for (int i = vehiculo.getModelo(); i < getLocalDate.getYear(); i++) {
                vehiculo.setPrecio(vehiculo.getPrecio() * 0.9);
            }
        }).collect(Collectors.groupingBy(Vehiculo::getPrecio));
    }

    private static Map<String, String> getStringStringMap(List<Vehiculo> v) {
        Map<String, String> mapCar = new HashMap<>();
        mapCar.put("mas_costoso", v.stream().map(Vehiculo::getPrecio)
                .max(Double::compareTo).orElseThrow().toString());
        mapCar.put("menos_costoso", v.stream().map(Vehiculo::getPrecio)
                .min(Double::compareTo).orElseThrow().toString());
        mapCar.put("promedio", String.valueOf(v.stream().map(Vehiculo::getPrecio)
                .flatMapToDouble(DoubleStream::of).average().orElseThrow()));
        mapCar.put("suma", String.valueOf(v.stream().map(Vehiculo::getPrecio)
                .flatMapToDouble(DoubleStream::of).sum()));
        return mapCar;
    }

    private static Map<Integer, List<String>> getByBrandAndByColor(List<Vehiculo> list, String marca) {
        Map<Integer, List<String>> result = new HashMap<>();
        list.stream()
                .filter(vehiculo -> vehiculo.getMarca().equals(marca))
                .collect(Collectors.groupingBy(Vehiculo::getModelo))
                .forEach((clave, valor) -> result.put(clave, valor.stream().map(Vehiculo::getColor)
                        .distinct().collect(Collectors.toList())));
        return result;
    }

    @Override
    public void run(String... args) throws Exception {
        String json = getStringFromFileName();

        /* Funci??n que crea una lista de carros*/
        var carroList = getVehicles(json)
                .stream()
                .filter(vehiculo -> vehiculo.getTipo().equals("Carro"))
                .collect(Collectors.toList());
        log.info("Lista de Carros");
        carroList.forEach(vehiculo -> log.info(vehiculo.toString()));
        /* Funci??n que crea una lista de motos*/
        var motosList = getVehicles(json)
                .stream()
                .filter(vehiculo -> vehiculo.getTipo().equals("Moto"))
                .collect(Collectors.toList());
        log.info("Lista de Motos");
        motosList.forEach(vehiculo -> log.info(vehiculo.toString()));
        /* Funci??n que crea un Map con clave (Carro, Moto)*/
        var map = getVehicles(json)
                .stream()
                .collect(Collectors.groupingBy(Vehiculo::getTipo));
        log.info("Map Carro, Motos");
        log.info("Map Carro/Moto -> {}", map.toString());

        /* Funci??n que Calcule los impuestos */
        var vehicleWithImpuestos = getVehicles(json)
                .stream()
                .map(this::taxCalculator)
                .collect(Collectors.toList());
        log.info("Lista de veh??culos con impuestos");
        log.info("vehicleWithImpuestos -> {}", vehicleWithImpuestos);

        /*  Funci??n para establecer el estado del veh??culo ('Ok', 'Averiado'). Usar aleatorios. */
        var vehicleWithStatus = getVehicles(json)
                .stream()
                .map(this::setEstado)
                .collect(Collectors.toList());
        log.info("Lista del estado del veh??culo ('Ok', 'Averiado')");
        log.info("vehicleWithStatus -> {}", vehicleWithStatus);

        /*  Definir comportamiento Encender() seg??n el estado, retornar String con el formato: Veh??culo (placa):
        Encendido/No se pudo encender */
        /* Funci??n para obtener lista de los Strings al llamar al m??todo Encender() de cada veh??culo. */
        log.info("Lista de Encender() seg??n el estado del veh??culo ('Ok', 'Averiado')");
        getVehicles(json)
                .stream()
                .map(this::setEstado)
                .map(this::getEncender)
                .forEach(s -> log.info("Estado: {}", s));

        /*Funci??n para obtener los veh??culos clasificados seg??n el a??o de fabricaci??n.*/
        var modelGroup = getVehicles(json)
                .stream()
                .collect(Collectors.groupingBy(Vehiculo::getModelo));
        log.info("Lista de veh??culos clasificados seg??n el a??o de fabricaci??n");
        log.info("modelGroup -> {}", modelGroup.toString());

        /* Funci??n para obtener los veh??culos clasificados
        por su valor actual en mercado (al precio restarle el 10% por a??o). */
        var vehicleDeprecation = getVehicleByValue(getVehicles(json));
        log.info("Lista de veh??culos valor actual en mercado (al precio restarle el 10% por a??o).");
        log.info("vehicleDeprecation -> {}", vehicleDeprecation);

        /* Funci??n para clasificar los carros si son autom??ticos o no. */
        var vehicleTransmission = getVehicles(json)
                .stream()
                .filter(vehiculo -> vehiculo.getTipo().equals("Carro"))
                .collect(Collectors.groupingBy(Vehiculo::getTransmission));
        log.info("Lista de los carros si son autom??ticos o no.");
        log.info("vehicleTransmission -> {}", vehicleTransmission.toString());

        /* Funci??n para obtener datos sobre el precio de los carros (m??s costoso, menos costoso, promedio, suma). */
        log.info("Data car -> {}", this.getDataCar(getVehicles(json)));
        /* Funci??n para obtener datos sobre el precio de las motos (m??s costosa, menos costosa, promedio, suma). */
        log.info("Data Moto -> {}", this.getDataMoto(getVehicles(json)));

        /* Funci??n para obtener, dada una marca, el promedio de los precios de sus veh??culos, el m??s costoso y menos costoso. */
        var vehicleForBrand = this.getDataFromBrand(getVehicles(json)
                .stream()
                .filter(vehiculo -> vehiculo.getMarca().equals("Audi"))
                .collect(Collectors.toList()));
        log.info("Lista dada una marca, el promedio de los precios de sus veh??culos");
        log.info("Data Brand -> {}", vehicleForBrand);

        /* Funci??n para clasificar los colores disponibles por modelo, de una marca en espec??fico. */
        var modelGroupModelAndGroup = getByBrandAndByColorAndByModel(getVehicles(json));
        log.info("Lista de los colores disponibles por modelo, de una marca en espec??fico.");
        log.info("Crazy map -> {}", modelGroupModelAndGroup);
    }

    private Vehiculo taxCalculator(Vehiculo v) {
        v.setImpuesto(v.getPrecio() * 0.465);
        return v;
    }

    private Vehiculo setEstado(Vehiculo v) {
        var estados = Arrays.asList("OK", "Averiado");
        v.setEstado(estados.get(new Random().nextInt(estados.size())));
        return v;
    }

    private String getEncender(Vehiculo v) {
        return String.format("Vehiculo(%s): %s", setPlaca(),
                v.getEstado().equals("OK") ? "Encendido" : "No se pudo encender");
    }

    private Map<String, String> getDataCar(List<Vehiculo> v) {
        List<Vehiculo> carros = v.stream().filter(vehiculo -> vehiculo.getTipo().equals("Carro"))
                .collect(Collectors.toList());
        return getStringStringMap(carros);
    }

    private Map<String, String> getDataMoto(List<Vehiculo> v) {
        List<Vehiculo> motos = v.stream().filter(vehiculo -> vehiculo.getTipo().equals("Moto"))
                .collect(Collectors.toList());
        return getStringStringMap(motos);
    }

    private Map<String, String> getDataFromBrand(List<Vehiculo> v) {
        return getStringStringMap(v);
    }

    private Map<String, Map<Integer, List<String>>> getByBrandAndByColorAndByModel(List<Vehiculo> list) {
        Map<String, Map<Integer, List<String>>> result = new HashMap<>();
        list.stream().collect(Collectors.groupingBy(Vehiculo::getMarca))
                .forEach((marca, vehicleTemp) ->
                        result.put(marca, getByBrandAndByColor(vehicleTemp, marca)));
        return result;
    }

}
