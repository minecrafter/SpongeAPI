package org.spongepowered.api.item.merchant;

import java.util.List;
import java.util.Random;
import java.util.function.BiConsumer;

public interface TradeOfferListMutator extends BiConsumer<List<TradeOffer>, Random> {

    @Override
    void accept(List<TradeOffer> tradeOffers, Random random);

}
