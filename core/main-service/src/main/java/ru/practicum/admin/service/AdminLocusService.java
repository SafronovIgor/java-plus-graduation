package ru.practicum.admin.service;

import ru.practicum.dto.locus.LocusUpdateDto;
import ru.practicum.dto.locus.NewLocusDto;
import ru.practicum.model.Locus;

import java.util.List;

public interface AdminLocusService {
    void deleteLocus(Long locusId);

    Locus addLocus(NewLocusDto locus);

    List<Locus> getAllLoci();

    Locus getLocusById(Long locusId);

    Locus updateLocus(Long locusId, LocusUpdateDto locusUpdateDto);
}
