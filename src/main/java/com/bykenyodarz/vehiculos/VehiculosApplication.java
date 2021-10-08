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
public class VehiculosApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(VehiculosApplication.class, args);
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

    @Override
    public void run(String... args) throws Exception {
        String json = getStringFromFileName();

        /* Función que crea una lista de carros*/
        var carroList = getVehiculos(json)
                .stream()
                .filter(vehiculo -> vehiculo.getTipo().equals("Carro"))
                .collect(Collectors.toList());
        log.info("Lista de Carros");
        carroList.forEach(vehiculo -> log.info(vehiculo.toString()));
        /* Función que crea una lista de motos*/
        var motosList = getVehiculos(json)
                .stream()
                .filter(vehiculo -> vehiculo.getTipo().equals("Moto"))
                .collect(Collectors.toList());
        log.info("Lista de Motos");
        motosList.forEach(vehiculo -> log.info(vehiculo.toString()));
        /* Función que crea un Map con clave (Carro, Moto)*/
        var map = getVehiculos(json)
                .stream()
                .collect(Collectors.groupingBy(Vehiculo::getTipo));
        log.info("Map -> {}", map.toString());

        /* Función que Calcule los impuestos */
        var vehicleWithImpuestos = getVehiculos(json)
                .stream()
                .map(this::taxCalculator)
                .collect(Collectors.toList());
        log.info("vehicleWithImpuestos -> {}", vehicleWithImpuestos);

        /*  Función para establecer el estado del vehículo ('Ok', 'Averiado'). Usar aleatorios. */
        var vehicleWithStatus = getVehiculos(json)
                .stream()
                .map(this::setEstado)
                .collect(Collectors.toList());
        log.info("vehicleWithStatus -> {}", vehicleWithStatus);

        /*  Definir comportamiento Encender() según el estado, retornar String con el formato: Vehículo (placa):
        Encendido/No se pudo encender */
        /* Función para obtener lista de los Strings al llamar al método Encender() de cada vehículo. */
        getVehiculos(json)
                .stream()
                .map(this::setEstado)
                .map(this::getEncender)
                .forEach(s -> log.info("Estado: {}", s));

        /*Función para obtener los vehículos clasificados según el año de fabricación.*/
        var modelGroup = getVehiculos(json)
                .stream()
                .collect(Collectors.groupingBy(Vehiculo::getModelo));
        log.info("modelGroup -> {}", modelGroup.toString());

        /* Función para obtener los vehículos clasificados
        por su valor actual en mercado (al precio restarle el 10% por año). */
        var vehicleDeprecation = getVehicleByValue(getVehiculos(json));
        log.info("vehicleDeprecation -> {}", vehicleDeprecation);

        /* Función para clasificar los carros si son automáticos o no. */
        var vehicleTransmission = getVehiculos(json)
                .stream()
                .filter(vehiculo -> vehiculo.getTipo().equals("Carro"))
                .collect(Collectors.groupingBy(Vehiculo::getTransmission));
        log.info("vehicleTransmission -> {}", vehicleTransmission.toString());

        /* Función para obtener datos sobre el precio de los carros (más costoso, menos costoso, promedio, suma). */
        log.info("Data car -> {}", this.getDataCar(getVehiculos(json)));
        /* Función para obtener datos sobre el precio de las motos (más costosa, menos costosa, promedio, suma). */
        log.info("Data Moto -> {}", this.getDataMoto(getVehiculos(json)));

        /* Función para obtener, dada una marca, el promedio de los precios de sus vehículos, el más costoso y menos costoso. */
        var vehicleForBrand = this.getDataFromBrand(getVehiculos(json)
                .stream()
                .filter(vehiculo -> vehiculo.getMarca().equals("Audi"))
                .collect(Collectors.toList()));
        log.info("Data Brand -> {}", vehicleForBrand);

        /* Función para clasificar los colores disponibles por modelo, de una marca en específico. */
        var modelGroupModelAndGroup = getByBrandAndByColorAndByModel(getVehiculos(json));
        log.info("Crazy map -> {}", modelGroupModelAndGroup);
    }

    private List<Vehiculo> getVehiculos(String json) throws JsonProcessingException {
        final ObjectMapper objectMapper = new ObjectMapper();
        Vehiculo[] array = objectMapper.readValue(json, Vehiculo[].class);
        return Arrays.asList(array);
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


    private Map<Double, List<Vehiculo>> getVehicleByValue(List<Vehiculo> list) {
        LocalDate getLocalDate = LocalDate.now();
        return list.stream().peek(vehiculo -> {
            for (int i = vehiculo.getModelo(); i < getLocalDate.getYear(); i++) {
                vehiculo.setPrecio(vehiculo.getPrecio() * 0.9);
            }
        }).collect(Collectors.groupingBy(Vehiculo::getPrecio));
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

    private Map<String, String> getStringStringMap(List<Vehiculo> v) {
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

    private Map<String, Map<Integer, List<String>>> getByBrandAndByColorAndByModel(List<Vehiculo> list) {
        Map<String, Map<Integer, List<String>>> result = new HashMap<>();
        list.stream().collect(Collectors.groupingBy(Vehiculo::getMarca))
                .forEach((marca, vehicleTemp) ->
                        result.put(marca, getByBrandAndByColor(vehicleTemp, marca)));
        return result;
    }

    private Map<Integer, List<String>> getByBrandAndByColor(List<Vehiculo> list, String marca) {
        Map<Integer, List<String>> result = new HashMap<>();
        list.stream()
                .filter(vehiculo -> vehiculo.getMarca().equals(marca))
                .collect(Collectors.groupingBy(Vehiculo::getModelo))
                .forEach((clave, valor) -> result.put(clave, valor.stream().map(Vehiculo::getColor)
                        .distinct().collect(Collectors.toList())));
        return result;
    }

}
