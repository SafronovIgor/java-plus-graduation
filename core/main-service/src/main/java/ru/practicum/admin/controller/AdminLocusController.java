package ru.practicum.admin.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.admin.service.AdminLocusService;
import ru.practicum.dto.locus.LocusUpdateDto;
import ru.practicum.dto.locus.NewLocusDto;
import ru.practicum.model.Locus;

import java.util.List;

@RestController
@RequestMapping("/admin/loci")
@RequiredArgsConstructor
@Validated
public class AdminLocusController {

    private final AdminLocusService adminLocusService;

    @PostMapping
    public ResponseEntity<Locus> addLocus(@RequestBody @Valid NewLocusDto locus) {
        return new ResponseEntity<>(adminLocusService.addLocus(locus), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<Locus>> getAllLoci() {
        return new ResponseEntity<>(adminLocusService.getAllLoci(), HttpStatus.OK);
    }

    @GetMapping("/{locusId}")
    public ResponseEntity<Locus> getLocusById(@PathVariable Long locusId) {
        return new ResponseEntity<>(adminLocusService.getLocusById(locusId), HttpStatus.OK);
    }

    @PatchMapping("/{locusId}")
    public ResponseEntity<Locus> updateLocus(@PathVariable Long locusId,
                                             @RequestBody @Valid LocusUpdateDto locusUpdateDto) {
        return new ResponseEntity<>(adminLocusService.updateLocus(locusId, locusUpdateDto), HttpStatus.OK);
    }

    @DeleteMapping("/{locusId}")
    public ResponseEntity<Void> deleteLocus(@PathVariable Long locusId) {
        adminLocusService.deleteLocus(locusId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
