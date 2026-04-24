package com.lyk.coursearrange.schedule.engine.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 种群对象。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SchedulingPopulation {

    private int generation;

    private List<SchedulingChromosome> chromosomes;
}
