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
package org.spongepowered.api.item.inventory;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.spongepowered.api.util.weighted.VariableAmount.baseWithRandomAddition;
import static org.spongepowered.api.util.weighted.VariableAmount.fixed;

import com.google.common.collect.ImmutableList;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.manipulator.mutable.item.EnchantmentData;
import org.spongepowered.api.data.meta.ItemEnchantment;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.data.value.mutable.ListValue;
import org.spongepowered.api.item.Enchantment;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.util.Tuple;
import org.spongepowered.api.util.weighted.VariableAmount;
import org.spongepowered.api.util.weighted.WeightedTable;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public final class ItemStackBuilderPopulators {

    public static BiConsumer<ItemStack.Builder, Random> itemStack(ItemStackSnapshot snapshot) {
        checkNotNull(snapshot, "ItemStackSnapshot cannot be null!");
        return (builder, random) -> builder.fromSnapshot(snapshot);
    }

    public static BiConsumer<ItemStack.Builder, Random> itemStacks(ItemStackSnapshot snapshot, ItemStackSnapshot... snapshots) {
        checkNotNull(snapshot, "ItemStackSnapshot cannot be null!");
        final WeightedTable<ItemStackSnapshot> table = new WeightedTable<>(1);
        table.add(snapshot, 1);
        for (ItemStackSnapshot stackSnapshot : snapshots) {
            table.add(checkNotNull(stackSnapshot, "ItemStackSnapshot cannot be null!"), 1);
        }
        return (builder, random) -> builder.fromSnapshot(table.get(random).get(0));
    }

    public static BiConsumer<ItemStack.Builder, Random> item(ItemType itemType) {
        checkNotNull(itemType, "ItemType cannot be null!");
        return (builder, random) -> builder.itemType(itemType);
    }

    public static BiConsumer<ItemStack.Builder, Random> item(Supplier<ItemType> supplier) {
        checkNotNull(supplier, "Supplier cannot be null!");
        return (builder, random) -> builder.itemType(checkNotNull(supplier.get(), "Supplier returned a null ItemType"));
    }

    public static BiConsumer<ItemStack.Builder, Random> items(ItemType itemType, ItemType... itemTypes) {
        return items(ImmutableList.<ItemType>builder().add(itemType).addAll(Arrays.asList(itemTypes)).build());
    }

    public static BiConsumer<ItemStack.Builder, Random> items(final Collection<ItemType> itemTypes) {
        final ImmutableList<ItemType> copiedItemTypes = ImmutableList.copyOf(itemTypes);
        return (builder, random) -> builder.itemType(copiedItemTypes.get(random.nextInt(copiedItemTypes.size())));
    }

    public static BiConsumer<ItemStack.Builder, Random> items(final Supplier<Collection<ItemType>> supplier) {
        final ImmutableList<ItemType> collection = ImmutableList.copyOf(supplier.get());
        return (builder, random) -> {
            builder.itemType(collection.get(random.nextInt(collection.size())));
        };
    }

    public static BiConsumer<ItemStack.Builder, Random> quantity(VariableAmount amount) {
        checkNotNull(amount, "VariableAmount cannot be null!");
        return (builder, random) -> builder.quantity(amount.getFlooredAmount(random));
    }

    public static BiConsumer<ItemStack.Builder, Random> quantity(Supplier<VariableAmount> supplier) {
        checkNotNull(supplier, "Supplier cannot be null!");
        return (builder, random) -> builder.quantity(supplier.get().getFlooredAmount(random));
    }

    public static <E> BiConsumer<ItemStack.Builder, Random> keyValue(Key<? extends BaseValue<E>> key, E value) {
        return (builder, random) -> {
            final ItemStack itemStack = builder.build();
            final DataTransactionResult dataTransactionResult = itemStack.offer(key, value);
            if (dataTransactionResult.isSuccessful()) {
                builder.from(itemStack);
            }
        };
    }

    public static <E> BiConsumer<ItemStack.Builder, Random> keyValues(Key<? extends BaseValue<E>> key, Iterable<E> values) {
        checkNotNull(values, "Iterable cannot be null!");
        checkNotNull(key, "Key cannot be null!");
        WeightedTable<E> tableEntries = new WeightedTable<>(1);
        for (E e : values) {
            tableEntries.add(checkNotNull(e, "Value cannot be null!"), 1);
        }
        return (builder, random) -> {
            final ItemStack itemStack = builder.build();
            final DataTransactionResult dataTransactionResult = itemStack.offer(key, tableEntries.get(random).get(0));
            if (dataTransactionResult.isSuccessful()) {
                builder.from(itemStack);
            }
        };
    }

    public static <E, V extends BaseValue<E>> BiConsumer<ItemStack.Builder, Random> value(V value) {
        return (builder, random) -> {
            final ItemStack itemStack = builder.build();
            final DataTransactionResult dataTransactionResult = itemStack.offer(value);
            if (dataTransactionResult.isSuccessful()) {
                builder.from(itemStack);
            }
        };
    }

    public static <E, V extends BaseValue<E>> BiConsumer<ItemStack.Builder, Random> values(Iterable<V> values) {
        WeightedTable<V> tableEntries = new WeightedTable<>(1);
        for (V value : values) {
            tableEntries.add(checkNotNull(value, "Value cannot be null!"), 1);
        }
        return ((builder, random) -> {
            final V value = tableEntries.get(random).get(0);
            final ItemStack itemStack = builder.build();
            final DataTransactionResult result = itemStack.offer(value);
            if (result.isSuccessful()) {
                builder.from(itemStack);
            }
        });
    }

    public static BiConsumer<ItemStack.Builder, Random> data(DataManipulator<?, ?> manipulator) {
        checkNotNull(manipulator, "DataManipulator cannot be null!");
        return (builder, random) -> builder.itemData(manipulator);
    }

    public static BiConsumer<ItemStack.Builder, Random> data(Collection<DataManipulator<?, ?>> manipulators) {
        checkNotNull(manipulators, "DataManipulators cannot be null!");
        final WeightedTable<DataManipulator<?, ?>> table = new WeightedTable<>();
        manipulators.forEach(manipulator -> table.add(checkNotNull(manipulator, "DataManipulator cannot be null!"), 1));
        return (builder, random) -> builder.itemData(table.get(random).get(0));
    }

    public static BiConsumer<ItemStack.Builder, Random> data(Collection<DataManipulator<?, ?>> manipulators, VariableAmount rolls) {
        checkNotNull(manipulators, "Manipulators cannot be null!");
        checkNotNull(rolls, "VariableAmount cannot be null!");
        final ImmutableList<DataManipulator<?, ?>> copied = ImmutableList.copyOf(manipulators);
        final WeightedTable<DataManipulator<?, ?>> table = new WeightedTable<>();
        table.setRolls(rolls);
        copied.forEach(manipulator1 -> table.add(manipulator1, 1));
        return (builder, random) -> {
            table.get(random).forEach(builder::itemData);
        };
    }

    public static BiConsumer<ItemStack.Builder, Random> data(WeightedTable<DataManipulator<?, ?>> weightedTable) {
        checkNotNull(weightedTable, "WeightedTable cannot be null!");
        return (builder, random) -> weightedTable.get(random).forEach(builder::itemData);
    }

    public static BiConsumer<ItemStack.Builder, Random> enchantment(Enchantment enchantment) {
        return enchantment(fixed(1), enchantment);
    }

    public static BiConsumer<ItemStack.Builder, Random> enchantment(VariableAmount level, Enchantment enchantment) {
        checkNotNull(level, "VariableAmount cannot be null!");
        checkNotNull(enchantment, "Enchantment cannot be null!");
        return enchantments(fixed(1), ImmutableList.of(new Tuple<>(enchantment, level)));
    }

    public static BiConsumer<ItemStack.Builder, Random> enchantmentsWithVanillaLevelVariance(Collection<Enchantment> enchantments) {
        return enchantmentsWithVanillaLevelVariance(fixed(1), ImmutableList.copyOf(enchantments));
    }

    public static BiConsumer<ItemStack.Builder, Random> enchantmentsWithVanillaLevelVariance(VariableAmount amount, Enchantment enchantment, Enchantment... enchantments) {
        return enchantmentsWithVanillaLevelVariance(amount, ImmutableList.<Enchantment>builder().add(enchantment).addAll(Arrays.asList(enchantments)).build());
    }

    public static BiConsumer<ItemStack.Builder, Random> enchantmentsWithVanillaLevelVariance(VariableAmount amount, Collection<Enchantment> itemEnchantments) {
        checkNotNull(amount, "Variable amount cannot be null!");
        checkNotNull(itemEnchantments, "Enchantment collection cannot be null!");
        List<Tuple<Enchantment, VariableAmount>> list = itemEnchantments.stream()
                .map(enchantment -> {
                    checkNotNull(enchantment, "Enchantment cannot be null!");
                    final int minimum = enchantment.getMinimumLevel();
                    final int maximum = enchantment.getMaximumLevel();
                    return new Tuple<>(enchantment, baseWithRandomAddition(minimum, maximum - minimum));
                })
                .collect(Collectors.toList());
        return enchantments(amount, () -> list);
    }

    public static BiConsumer<ItemStack.Builder, Random> enchantments(VariableAmount amount, Collection<Tuple<Enchantment, VariableAmount>> enchantments) {
        return enchantments(amount, () -> enchantments);
    }

    public static BiConsumer<ItemStack.Builder, Random> enchantments(VariableAmount amount, Supplier<Collection<Tuple<Enchantment, VariableAmount>>> enchantments) {
        checkNotNull(enchantments, "Supplier cannot be null!");
        checkNotNull(amount, "VariableAmount cannot be null!");
        WeightedTable<Tuple<Enchantment, VariableAmount>> table = new WeightedTable<>();
        table.setRolls(amount);
        final ImmutableList<Tuple<Enchantment, VariableAmount>> list = ImmutableList.copyOf(enchantments.get());
        list.forEach(tupleSupplier -> table.add(tupleSupplier, 1));
        return (builder, random) -> {
            final ItemStack temp = builder.build();
            final EnchantmentData enchantmentData = temp.getOrCreate(EnchantmentData.class).get();
            ListValue<ItemEnchantment> enchantmentListValue = enchantmentData.enchantments();
            table.get(random).stream()
                    .forEach(tuple -> {
                        final int level = tuple.getSecond().getFlooredAmount(random);
                        checkArgument(level > 0, "Enchantment levels must be greater than zero!");
                        final ItemEnchantment itemEnchantment = new ItemEnchantment(tuple.getFirst(), level);
                        enchantmentListValue.add(itemEnchantment);
                    });
            enchantmentData.set(enchantmentListValue);
            builder.itemData(enchantmentData);
        };
    }

    private ItemStackBuilderPopulators() {
    }

}
