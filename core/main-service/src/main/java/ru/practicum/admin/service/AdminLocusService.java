package ru.practicum.admin.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.admin.repository.AdminLocusRepository;
import ru.practicum.dto.locus.LocusMapper;
import ru.practicum.dto.locus.LocusUpdateDto;
import ru.practicum.dto.locus.NewLocusDto;
import ru.practicum.exception.NotFoundException;
import ru.practicum.model.Locus;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminLocusService {
    private final AdminLocusRepository adminLocusRepository;
    private final LocusMapper locusMapper;

    public void deleteLocus(Long locusId) {
        adminLocusRepository.deleteById(locusId);
    }

    public Locus addLocus(NewLocusDto locus) {
        return adminLocusRepository.save(locusMapper.toLocus(locus));
    }

    public List<Locus> getAllLoci() {
        return adminLocusRepository.findAll();
    }

    public Locus getLocusById(Long locusId) {
        return adminLocusRepository.findById(locusId).orElseThrow(() -> new NotFoundException("Locus not found"));
    }

    public Locus updateLocus(Long locusId, LocusUpdateDto locusUpdateDto) {
        Locus locus =
                adminLocusRepository.findById(locusId).orElseThrow(() -> new NotFoundException("Locus not found"));
        locus = locusMapper.updateLocus(locus, locusUpdateDto);
        return adminLocusRepository.save(locus);
    }
}
