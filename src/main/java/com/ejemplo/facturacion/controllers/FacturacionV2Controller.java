package com.ejemplo.facturacion.controllers;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.ejemplo.facturacion.services.FacturaService;
import com.ejemplo.facturacion.valueobjects.Factura;
import com.ejemplo.facturacion.valueobjects.Orden;

@RestController
public class FacturacionV2Controller {
    @Autowired
    FacturaService facturaService;

    @PostMapping("/v2/factura")
    public ResponseEntity<String> calcularFactura(@RequestBody Orden orden) throws InterruptedException {
        String idFactura = facturaService.iniciarFacturaAsincrona(orden);

        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add(HttpHeaders.LOCATION, "/v2/factura/" + idFactura);

        return new ResponseEntity<>(idFactura, headers, HttpStatus.ACCEPTED);
    }

    @GetMapping("/v2/factura/{idFactura}")
    public ResponseEntity<Factura> buscarFactura(@PathVariable String idFactura) {
        Optional<Factura> facturaOptional = facturaService.obtenerFacturaAsincrona(idFactura);

        if (facturaOptional == null) {
            // El ID de factura no existe
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        if (facturaOptional.isEmpty()) {
            // La factura está en proceso de creación
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }

        // La factura ha sido generada
        return new ResponseEntity<>(facturaOptional.get(), HttpStatus.OK);
    }
}
