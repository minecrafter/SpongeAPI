/*
 * This file is part of SpongeAPI, licensed under the MIT License (MIT).
 *
 * Copyright (c) SpongePowered <https://www.spongepowered.org>
 * Copyright (c) contributors
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
package org.spongepowered.api.item.merchant;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;
import org.spongepowered.api.data.type.Career;
import org.spongepowered.api.util.GuavaCollectors;

import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public interface VillagerRegistry {

    /**
     * Gets an immutable {@link Multimap} of the {@link Career}'s registered
     * {code level} to {@link TradeOfferGenerator}s. Note that the map is
     * immutable and cannot be modified directly.
     *
     * @param career The career
     * @return The immutable multimap
     */
    Multimap<Integer, TradeOfferGenerator> getTradeOfferLevelMap(Career career);

    /**
     * Gets the available {@link TradeOfferGenerator}s for the desired
     * {@link Career} and {@code level}.
     *
     * @param career The career
     * @param level The level
     * @return The collection of trade offer generators, if available
     */
    default Collection<TradeOfferGenerator> getGenerators(Career career, int level) {
        final Multimap<Integer, TradeOfferGenerator> map = getTradeOfferLevelMap(checkNotNull(career, "Career cannot be null!"));
        final Collection<TradeOfferGenerator> generators = map.get(level);
        return ImmutableList.copyOf(generators);
    }

    /**
     * Adds the provided {@link TradeOfferGenerator} for the given
     * {@link Career} and {@code level}. Note that the level
     * must be at least 1. There can be multiple {@link TradeOfferGenerator}s
     * per level.
     *
     * @param career The career
     * @param level The level
     * @param generator The generator to register
     * @return This registry, for chaining
     */
    VillagerRegistry addGenerator(Career career, int level, TradeOfferGenerator generator);

    /**
     * Adds the provided {@link TradeOfferGenerator}s for the given
     * {@link Career} and {@code level}. Note that the level
     * must be at least 1. There can be multiple {@link TradeOfferGenerator}s
     * per level.
     *
     * @param career The career
     * @param level The level
     * @param generator The generator to register
     * @param generators The additional generators
     * @return This registry, for chaining
     */
    VillagerRegistry addGenerators(Career career, int level, TradeOfferGenerator generator, TradeOfferGenerator... generators);

    /**
     * Sets the provided {@link TradeOfferGenerator} for the given
     * {@link Career} and {@code level}. Note that the level
     * must be at least 1. There can be multiple {@link TradeOfferGenerator}s
     * per level. Any previously provided {@link TradeOfferGenerator}s will
     * be erased.
     *
     * @param career The career
     * @param level The level
     * @param generators The generators to register
     * @return This registry, for chaining
     */
    VillagerRegistry setGenerators(Career career, int level, List<TradeOfferGenerator> generators);

    /**
     * Sets the provided {@link TradeOfferGenerator} for the given
     * {@link Career} and {@code level}. Note that the level
     * must be at least 1. There can be multiple {@link TradeOfferGenerator}s
     * per level. Any previously provided {@link TradeOfferGenerator}s will
     * be erased.
     *
     * @param career The career
     * @param generatorMap The generator map
     * @return This registry, for chaining
     */
    VillagerRegistry setGenerators(Career career, Multimap<Integer, TradeOfferGenerator> generatorMap);

    /**
     * Generates a new {@link List} of {@link TradeOffer}s based on the
     * provided {@link Career}, {@code level}, and {@link Random}.
     *
     * @param career The career
     * @param level The level
     * @param random The random
     * @return The generated list of trade offers
     */
    default Collection<TradeOffer> generateTradeOffers(Career career, int level, Random random) {
        checkNotNull(random, "Random cannot be null!");
        return getGenerators(career, level).stream()
                .map(generator -> generator.apply(random))
                .collect(GuavaCollectors.toImmutableList());
    }

    /**
     * Populates the provided {@link List} of {@link TradeOffer}s with
     * potentially new {@link TradeOffer}s based on the
     * {@link TradeOfferGenerator}s and {@code level}. If there are no
     * {@link TradeOfferGenerator}s registered for the desired level
     * and {@link Career}, the list remains unmodified.
     *
     * @param currentOffers The current offers
     * @param career The career
     * @param level The level
     * @param random The random to use
     * @return The list of offers modified
     */
    default List<TradeOffer> populateOffers(List<TradeOffer> currentOffers, Career career, int level, Random random) {
        Collection<TradeOfferGenerator> generators = getGenerators(career, level);
        currentOffers.addAll(generators.stream()
                .map(generator -> generator.apply(random))
                .collect(Collectors.toList()));
        return currentOffers;
    }

}
