/*
 * The MIT License
 *
 * Copyright 2014 Miroslav Cupak (mirocupak@gmail.com).
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.dnastack.beacon.core.utils;

import com.dnastack.beacon.core.adapter.exception.BeaconAlleleRequestException;
import org.ga4gh.beacon.BeaconAlleleRequest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

/**
 * Utils for query manipulation.
 *
 * @author Miroslav Cupak (mirocupak@gmail.com)
 * @version 1.0
 */
public class QueryUtils {

    private static final Map<Reference, String> chromMapping = new HashMap<>();

    static {
        chromMapping.put(Reference.HG38, "GRCh38");
        chromMapping.put(Reference.HG19, "GRCh37");
        chromMapping.put(Reference.HG18, "NCBI36");
        chromMapping.put(Reference.HG17, "NCBI35");
        chromMapping.put(Reference.HG16, "NCBI34");

    }

    /**
     * Generates a canonical chrom ID.
     *
     * @param chrom chromosome
     * @return normalized chromosome value
     */
    public static Chromosome normalizeReference(String chrom) {
        // parse chrom value
        if (chrom != null) {
            String orig = chrom.toUpperCase();
            for (Chromosome c : Chromosome.values()) {
                if (orig.endsWith(c.toString())) {
                    return c;
                }
            }
        }

        return null;
    }

    public static boolean isValidReference(String referenceName) {
        if (referenceName == null) {
            return false;
        }
        String orig = referenceName.toUpperCase();
        for (Chromosome c : Chromosome.values()) {
            if (orig.endsWith(c.toString())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Converts 0-based position to 1-based position.
     *
     * @param pos 0-based position
     * @return 1-based position
     */
    public static Long normalizePosition(Long pos) {
        if (pos == null) {
            return null;
        }
        return ++pos;
    }

    /**
     * Generate a canonical allele string.
     *
     * @param allele denormalized allele
     * @return normalized allele
     */
    public static String normalizeAllele(String allele) {
        if (allele == null || allele.isEmpty()) {
            return null;
        }

        String res = allele.toUpperCase();
        if (res.equals("DEL") || res.equals("INS")) {
            return res.substring(0, 1);
        }
        if (Pattern.matches("([D,I])|([A,C,T,G]+)", res)) {
            return res;
        }

        return null;
    }

    /**
     * Generate a canonical genome representation (hg*).
     *
     * @param ref denormalized genome
     * @return normalized genome
     */
    public static Reference normalizeAssembly(String ref) {
        if (ref == null || ref.isEmpty()) {
            return null;
        }

        for (Reference s : chromMapping.keySet()) {
            if (s.toString().equalsIgnoreCase(ref)) {
                return s;
            }
        }
        for (Entry<Reference, String> e : chromMapping.entrySet()) {
            if (e.getValue().equalsIgnoreCase(ref)) {
                return e.getKey();
            }
        }

        return null;
    }

    /**
     * @param referenceName           name of chromosome or contig
     * @param start                   0-based start position
     * @param referenceBases          reference bases
     * @param alternateBases          alternate bases
     * @param assemblyId              genome assemlbly build id
     * @param datasetIds
     * @param includeDatasetResponses
     * @return
     */
    public static BeaconAlleleRequest getQuery(String referenceName, Long start, String referenceBases, String alternateBases, String assemblyId, List<String> datasetIds, Boolean includeDatasetResponses) {
        Chromosome chrom = normalizeReference(referenceName);
        Reference assembly = normalizeAssembly(assemblyId);

        BeaconAlleleRequest request = new BeaconAlleleRequest();
        request.setReferenceName(chrom == null ? null : chrom.toString());
        request.setStart(start);
        request.setReferenceBases(normalizeAllele(referenceBases));
        request.setAlternateBases(normalizeAllele(alternateBases));
        request.setAssemblyId(assembly.toString());
        request.setDatasetIds(datasetIds);
        request.setIncludeDatasetResponses(includeDatasetResponses);

        return request;
    }

    /**
     * Normalize a beacon allele request, validating it first.
     *
     * @param request
     * @return
     * @throws BeaconAlleleRequestException
     */
    public static BeaconAlleleRequest normalizeRequest(BeaconAlleleRequest request) throws BeaconAlleleRequestException {
        validateRequest(request);

        request.setReferenceName(normalizeAllele(request.getReferenceBases()));
        request.setAlternateBases(normalizeAllele(request.getAlternateBases()));
        request.setAssemblyId(normalizeAssembly(request.getAssemblyId()).toString());
        request.setReferenceName(normalizeReference(request.getReferenceName()).toString());

        return request;
    }

    /**
     * Validates a beacon allele request
     *
     * @param request
     */
    public static void validateRequest(BeaconAlleleRequest request) throws BeaconAlleleRequestException {

        if (request.getReferenceName() != null || !isValidReference(request.getReferenceName())) {
            throw new BeaconAlleleRequestException("Invalid reference passed in request", Reason.INVALID_REQUEST, request);
        } else if (request.getStart() == null || request.getStart() < 0) {
            throw new BeaconAlleleRequestException("Invalid start position in request", Reason.INVALID_REQUEST, request);
        } else if (request.getReferenceBases() == null) {
            throw new BeaconAlleleRequestException("Invalid reference bases in request", Reason.INVALID_REQUEST, request);
        } else if (request.getAlternateBases() == null) {
            throw new BeaconAlleleRequestException("Invalid alternate bases in request", Reason.INVALID_REQUEST, request);
        } else if (request.getAssemblyId() == null) {
            throw new BeaconAlleleRequestException("Invalid assembly", Reason.INVALID_REQUEST, request);
        }

    }

}
