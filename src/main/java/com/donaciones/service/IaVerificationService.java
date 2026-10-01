package com.donaciones.service;

import com.donaciones.dto.RespuestaIaDniDTO;
import org.springframework.web.multipart.MultipartFile;

public interface IaVerificationService {

    RespuestaIaDniDTO verificarFotoDni(MultipartFile archivo);

}
