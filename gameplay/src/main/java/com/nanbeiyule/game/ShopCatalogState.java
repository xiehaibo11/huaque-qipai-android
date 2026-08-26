package com.nanbeiyule.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class ShopCatalogState {
    private final Map<ShopCategory, List<ShopProduct>> products;
    private final ShopCategory selectedCategory;
    private final ShopPropSection selectedPropSection;
    private final ShopHotSection selectedHotSection;
    private final ShopInteractionSection selectedInteractionSection;
    private final ShopDecorationSection selectedDecorationSection;

    private ShopCatalogState(
            Map<ShopCategory, List<ShopProduct>> products,
            ShopCategory selectedCategory,
            ShopPropSection selectedPropSection,
            ShopHotSection selectedHotSection,
            ShopInteractionSection selectedInteractionSection,
            ShopDecorationSection selectedDecorationSection) {
        this.products = products;
        this.selectedCategory = Objects.requireNonNull(selectedCategory, "selectedCategory");
        this.selectedPropSection =
                Objects.requireNonNull(selectedPropSection, "selectedPropSection");
        this.selectedHotSection =
                Objects.requireNonNull(selectedHotSection, "selectedHotSection");
        this.selectedInteractionSection =
                Objects.requireNonNull(selectedInteractionSection, "selectedInteractionSection");
        this.selectedDecorationSection =
                Objects.requireNonNull(selectedDecorationSection, "selectedDecorationSection");
    }

    public static ShopCatalogState empty() {
        return create(Collections.emptyMap());
    }

    public static ShopCatalogState create(Map<ShopCategory, List<ShopProduct>> source) {
        Objects.requireNonNull(source, "source");
        EnumMap<ShopCategory, List<ShopProduct>> copied = new EnumMap<>(ShopCategory.class);
        for (ShopCategory category : ShopCategory.ordered()) {
            List<ShopProduct> input = source.get(category);
            ArrayList<ShopProduct> categoryProducts =
                    input == null ? new ArrayList<>() : new ArrayList<>(input);
            for (ShopProduct product : categoryProducts) {
                if (product.category() != category) {
                    throw new IllegalArgumentException(
                            "Product " + product.productCode() + " is under the wrong category");
                }
            }
            copied.put(category, Collections.unmodifiableList(categoryProducts));
        }
        return new ShopCatalogState(
                Collections.unmodifiableMap(copied),
                ShopCategory.HOT_RECOMMENDATION,
                ShopPropSection.RECORDER,
                ShopHotSection.VALUE_RECOMMENDATION,
                ShopInteractionSection.EMOTICON,
                ShopDecorationSection.VEHICLE);
    }

    public ShopCatalogState select(String categoryId) {
        return select(ShopCategory.fromId(categoryId));
    }

    public ShopCatalogState select(ShopCategory category) {
        return new ShopCatalogState(
                products,
                Objects.requireNonNull(category, "category"),
                selectedPropSection,
                selectedHotSection,
                selectedInteractionSection,
                selectedDecorationSection);
    }

    public ShopCatalogState selectPropSection(ShopPropSection section) {
        return new ShopCatalogState(
                products,
                selectedCategory,
                Objects.requireNonNull(section, "section"),
                selectedHotSection,
                selectedInteractionSection,
                selectedDecorationSection);
    }

    public ShopCatalogState selectHotSection(ShopHotSection section) {
        return new ShopCatalogState(
                products,
                selectedCategory,
                selectedPropSection,
                Objects.requireNonNull(section, "section"),
                selectedInteractionSection,
                selectedDecorationSection);
    }

    public ShopCatalogState selectInteractionSection(ShopInteractionSection section) {
        return new ShopCatalogState(
                products,
                selectedCategory,
                selectedPropSection,
                selectedHotSection,
                Objects.requireNonNull(section, "section"),
                selectedDecorationSection);
    }

    public ShopCatalogState selectDecorationSection(ShopDecorationSection section) {
        return new ShopCatalogState(
                products,
                selectedCategory,
                selectedPropSection,
                selectedHotSection,
                selectedInteractionSection,
                Objects.requireNonNull(section, "section"));
    }

    public ShopCategory selectedCategory() {
        return selectedCategory;
    }

    public List<ShopProduct> selectedProducts() {
        List<ShopProduct> selected = products(selectedCategory);
        if (selectedCategory == ShopCategory.HOT_RECOMMENDATION) {
            return selected.stream().filter(selectedHotSection::contains).toList();
        }
        if (selectedCategory == ShopCategory.DECORATION) {
            return selected.stream().filter(selectedDecorationSection::contains).toList();
        }
        if (selectedCategory == ShopCategory.INTERACTION) {
            return selected.stream().filter(selectedInteractionSection::contains).toList();
        }
        if (selectedCategory != ShopCategory.PROP) {
            return selected;
        }
        return selected.stream().filter(selectedPropSection::contains).toList();
    }

    public ShopPropSection selectedPropSection() {
        return selectedPropSection;
    }

    public ShopHotSection selectedHotSection() {
        return selectedHotSection;
    }

    public ShopInteractionSection selectedInteractionSection() {
        return selectedInteractionSection;
    }

    public ShopDecorationSection selectedDecorationSection() {
        return selectedDecorationSection;
    }

    public List<ShopProduct> products(ShopCategory category) {
        List<ShopProduct> result = products.get(Objects.requireNonNull(category, "category"));
        return result == null ? Collections.emptyList() : result;
    }

    public ShopProduct findProduct(String productCode) {
        if (productCode == null || productCode.isBlank()) {
            return null;
        }
        for (List<ShopProduct> categoryProducts : products.values()) {
            for (ShopProduct product : categoryProducts) {
                if (productCode.equals(product.productCode())) {
                    return product;
                }
            }
        }
        return null;
    }
}
