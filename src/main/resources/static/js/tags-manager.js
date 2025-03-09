/**
 * 标签管理器
 * 用于处理培训材料的标签添加、删除和显示
 */
class TagsManager {
    constructor(inputId, containerId) {
        this.tagsInput = document.getElementById(inputId);
        this.tagsContainer = document.getElementById(containerId);
        this.tags = [];

        this.init();
    }

    init() {
        // 初始化事件监听
        this.tagsInput.addEventListener('keydown', (e) => {
            if (e.key === 'Enter' || e.key === ',') {
                e.preventDefault();
                this.addTag();
            }
        });

        this.tagsInput.addEventListener('blur', () => {
            this.addTag();
        });

        // 如果输入框已有值，初始化标签
        if (this.tagsInput.value) {
            this.setTags(this.tagsInput.value.split(',').map(tag => tag.trim()).filter(tag => tag));
        }
    }

    addTag(tagValue) {
        const value = tagValue || this.tagsInput.value.trim();
        if (!value) return;

        // 处理可能包含多个标签（逗号分隔）的情况
        const newTags = value.split(',').map(tag => tag.trim()).filter(tag => tag);

        newTags.forEach(tag => {
            if (!this.tags.includes(tag)) {
                this.tags.push(tag);
            }
        });

        this.tagsInput.value = '';
        this.renderTags();
        this.updateInputValue();
    }

    removeTag(tag) {
        this.tags = this.tags.filter(t => t !== tag);
        this.renderTags();
        this.updateInputValue();
    }

    setTags(tags) {
        this.tags = Array.isArray(tags) ? tags : [];
        this.renderTags();
        this.updateInputValue();
    }

    renderTags() {
        this.tagsContainer.innerHTML = '';

        this.tags.forEach(tag => {
            const tagElement = document.createElement('div');
            tagElement.className = 'tag-item';
            tagElement.innerHTML = `
                <span class="tag-text">${tag}</span>
                <span class="remove-tag">&times;</span>
            `;

            tagElement.querySelector('.remove-tag').addEventListener('click', () => {
                this.removeTag(tag);
            });

            this.tagsContainer.appendChild(tagElement);
        });
    }

    updateInputValue() {
        // 更新隐藏输入框的值，用于表单提交
        this.tagsInput.value = this.tags.join(',');
    }

    getTags() {
        return this.tags;
    }

    clearTags() {
        this.tags = [];
        this.renderTags();
        this.updateInputValue();
    }
}

// 初始化标签管理器
document.addEventListener('DOMContentLoaded', () => {
    const tagsManager = new TagsManager('tags', 'tagsContainer');
});