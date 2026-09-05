package com.esprit.formation.catalogue.service;

import com.esprit.formation.catalogue.domain.Category;
import com.esprit.formation.catalogue.dto.CatalogueMapper;
import com.esprit.formation.catalogue.dto.CategoryRequest;
import com.esprit.formation.catalogue.dto.CategoryResponse;
import com.esprit.formation.catalogue.repository.CategoryRepository;
import com.esprit.formation.catalogue.repository.FormationRepository;
import com.esprit.formation.common.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final FormationRepository formationRepository;

    public CategoryService(CategoryRepository categoryRepository, FormationRepository formationRepository) {
        this.categoryRepository = categoryRepository;
        this.formationRepository = formationRepository;
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> findAll() {
        return categoryRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public CategoryResponse findById(Long id) {
        return toResponse(get(id));
    }

    public CategoryResponse create(CategoryRequest request) {
        String name = request.getName().trim();
        if (categoryRepository.existsByNameIgnoreCase(name)) {
            throw new ApiException(HttpStatus.CONFLICT, "Une catégorie avec ce nom existe déjà");
        }
        Category category = new Category();
        category.setName(name);
        category.setDescription(request.getDescription());
        return toResponse(categoryRepository.save(category));
    }

    public CategoryResponse update(Long id, CategoryRequest request) {
        Category category = get(id);
        String name = request.getName().trim();
        if (categoryRepository.existsByNameIgnoreCaseAndIdNot(name, id)) {
            throw new ApiException(HttpStatus.CONFLICT, "Une catégorie avec ce nom existe déjà");
        }
        category.setName(name);
        category.setDescription(request.getDescription());
        return toResponse(category);
    }

    public void delete(Long id) {
        Category category = get(id);
        if (formationRepository.existsByCategoryId(id)) {
            throw new ApiException(HttpStatus.CONFLICT, "Impossible de supprimer une catégorie utilisée par des formations");
        }
        categoryRepository.delete(category);
    }

    private Category get(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Catégorie introuvable"));
    }

    private CategoryResponse toResponse(Category category) {
        CategoryResponse response = CatalogueMapper.toCategory(category);
        response.setFormationCount(formationRepository.countByCategoryId(category.getId()));
        return response;
    }
}
