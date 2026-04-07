package co.habitarinmobiliaria.middleware_service.service;

import co.habitarinmobiliaria.middleware_service.client.SirvClient;
import co.habitarinmobiliaria.middleware_service.exception.ErrorExternoException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import co.habitarinmobiliaria.middleware_service.dtos.sirv.SirvTokenRequestDTO;
import co.habitarinmobiliaria.middleware_service.dtos.sirv.SirvTokenResponseDTO;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SirvService {

}
