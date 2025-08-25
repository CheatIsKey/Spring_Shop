package capstone.capstone_shop.service;

import capstone.capstone_shop.domain.Category;
import capstone.capstone_shop.domain.item.CategoryView;
import capstone.capstone_shop.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public List<Category> findAll() {
        return categoryRepository.findAll();
    }

    public List<CategoryView> findAllDto() {
        List<Category> all = categoryRepository.findAll();
        Map<Long, String> nameById = new ConcurrentHashMap<>();

        for (Category c : all)
            nameById.put(c.getId(), c.getName());

        List<CategoryView> result = new ArrayList<>();
        for (Category c : all) {
            String parentName = (c.getParent() != null) ? nameById.get(c.getParent().getId()) : null;
            result.add(new CategoryView(c.getId(), c.getName(), parentName));
        }

        return result;
    }

    @Transactional
    public Long create(String name, Long parentId) {
        Category newCategory;

        if (parentId == null) {
            newCategory = Category.createRoot(name);
        } else {
            Category parent = categoryRepository.findById(parentId)
                    .orElseThrow(() -> new IllegalArgumentException("상위 카테고리를 찾을 수 없습니다."));
            newCategory = Category.createChild(name, parent);
        }

        categoryRepository.save(newCategory);
        return newCategory.getId();
    }

    @Transactional
    public void delete(Long id) {
        categoryRepository.deleteById(id);
    }

}
