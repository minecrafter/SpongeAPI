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

import org.spongepowered.api.Sponge;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackGenerator;
import org.spongepowered.api.util.ResettableBuilder;
import org.spongepowered.api.util.weighted.VariableAmount;

import java.util.Random;
import java.util.function.Function;

import javax.annotation.Nullable;

/**
 * Represents a generator to create {@link TradeOffer}s with a bit of randomization
 * based on {@link ItemStackGenerator}s for populating {@link ItemStack}s and
 * finally generating a {@link TradeOffer}.
 *
 * <p>The primary use of this, and why the {@link Random} must be provided as
 * part of the {@link Function} signature is that during multiple world instances,
 * there's different {@link Random} instances instantiated, and more can be provided
 * without the necessity to change the generator. One advantage to using a generator
 * is the ability to provide some "randomization" or "chance" on the various aspects
 * of the generated {@link TradeOffer} versus creating a static non-changing offer.
 * Normally, the vanilla {@link TradeOffer}s are using a similar generator with
 * limited scopes of what the {@link ItemStack} can be customized as.</p>
 */
public interface TradeOfferGenerator extends Function<Random, TradeOffer> {

    /**
     * Gets a new {@link Builder} to create a new {@link TradeOfferGenerator}.
     *
     * @return
     */
    static Builder builder() {
        return Sponge.getRegistry().createBuilder(Builder.class);
    }

    interface Builder extends ResettableBuilder<TradeOfferGenerator, Builder> {

        /**
         * Sets the {@link ItemStackGenerator} for creating
         * @param generator
         * @return
         */
        Builder setItemGenerator(ItemStackGenerator generator);

        Builder setSecondGenerator(@Nullable ItemStackGenerator generator);

        Builder setSellingGenerator(ItemStackGenerator sellingGenerator);

        Builder experienceChance(double experienceChance);

        Builder startingUses(VariableAmount amount);

        Builder maxUses(VariableAmount amount);

        TradeOfferGenerator build();

    }

}
