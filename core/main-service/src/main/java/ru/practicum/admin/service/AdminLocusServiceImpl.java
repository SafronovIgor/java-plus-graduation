package ru.practicum.admin.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
@Transactional(readOnly = true)
public class AdminLocusServiceImpl implements AdminLocusService {
    private final AdminLocusRepository adminLocusRepository;
    private final LocusMapper locusMapper;

    @Override
    @Transactional
    public void deleteLocus(Long locusId) {
        adminLocusRepository.deleteById(locusId);
    }

    @Override
    @Transactional
    public Locus addLocus(NewLocusDto locus) {
        return adminLocusRepository.save(locusMapper.toLocus(locus));
    }

    @Override
    public List<Locus> getAllLoci() {
        return adminLocusRepository.findAll();
    }

    @Override
    public Locus getLocusById(Long locusId) {
        return adminLocusRepository.findById(locusId).orElseThrow(() -> new NotFoundException("Locus not found"));
    }

    @Override
    @Transactional
    public Locus updateLocus(Long locusId, LocusUpdateDto locusUpdateDto) {
        Locus locus = adminLocusRepository.findById(locusId).orElseThrow(
                () -> new NotFoundException("Locus not found")
        );
        return locusMapper.updateLocus(locus, locusUpdateDto);
    }
}