class TrainingManager {
    constructor() {

        // 获取CSRF令牌
        const csrfMeta = document.querySelector('meta[name="_csrf"]');
        const csrfHeaderMeta = document.querySelector('meta[name="_csrf_header"]');
        if (csrfMeta && csrfHeaderMeta) {
            this.csrfToken = csrfMeta.content;
            this.csrfHeader = csrfHeaderMeta.content;
        } else {
            console.error('CSRF meta tags not found');
            this.csrfToken = '';
            this.csrfHeader = '';
        }

        // 初始化加载状态
        this.loadingDiv = document.createElement('div');
        this.loadingDiv.className = 'loading-overlay';
        this.loadingDiv.innerHTML = '<div class="loading-spinner"></div>';
        document.body.appendChild(this.loadingDiv);

        // 初始化筛选参数
        const urlParams = new URLSearchParams(window.location.search);
        $('#statusFilter').val(urlParams.get('status') || '');
        $('#typeFilter').val(urlParams.get('type') || '');
        this.currentPage = urlParams.get('currentPage') || 1;

        // 初始化标签管理器
        this.tagsManager = new TagsManager('tags', 'tagsContainer');

        // 获取模态框元素
        this.uploadModal = document.getElementById('uploadModal');
        this.reviewModal = document.getElementById('reviewModal');
        this.uploadForm = document.getElementById('uploadForm');
        this.reviewForm = document.getElementById('reviewForm');
        this.editModal = document.getElementById('editModal');
        this.assignModal = document.getElementById('assignModal');

        this.initEventListeners();
        this.initAssignTrainingFeatures();
        console.info('TrainingManager initialized');
    }

    showLoading() {
        this.loadingDiv.style.display = 'flex';
    }

    hideLoading() {
        this.loadingDiv.style.display = 'none';
    }

    initEventListeners() {
        // 打开上传模态框
        document.getElementById('uploadMaterialBtn')?.addEventListener('click', () => {
            this.uploadModal.style.display = 'block';
            this.uploadForm.reset(); // 重置表单
            document.getElementById('passScoreGroup').style.display = 'none'; // 重置通过分数输入框显示状态
            this.tagsManager.clearTags(); // 清空标签
        });

        // 关闭模态框
        document.querySelectorAll('.close, #cancelUpload, #cancelReview').forEach(element => {
            element.addEventListener('click', () => {
                this.uploadModal.style.display = 'none';
                this.reviewModal.style.display = 'none';
            });
        });

        // 当选择测验类型时显示通过分数输入框
        document.getElementById('type')?.addEventListener('change', (e) => {
            const passScoreGroup = document.getElementById('passScoreGroup');
            passScoreGroup.style.display = e.target.value === 'QUIZ' ? 'block' : 'none';
        });

        // 文件类型验证
        document.getElementById('file')?.addEventListener('change', (e) => {
            const file = e.target.files[0];
            if (!file) return;

            const type = document.getElementById('type').value;
            const validTypes = {
                'DOCUMENT': ['application/pdf', 'application/msword', 'application/vnd.openxmlformats-officedocument.wordprocessingml.document'],
                'VIDEO': ['video/mp4', 'video/webm', 'video/ogg'],
                'QUIZ': ['application/json']
            };

            if (!validTypes[type].includes(file.type)) {
                alert('文件类型不匹配！请上传正确的文件类型。');
                e.target.value = ''; // 清空文件选择
            }
        });

        // 提交上传表单
        document.getElementById('submitUpload')?.addEventListener('click', this.handleUpload.bind(this));

        // 提交审核表单
        document.getElementById('submitReview')?.addEventListener('click', this.handleReview.bind(this));

        // 状态筛选器变化时刷新列表
        $('#statusFilter, #typeFilter').on('change', () => {
            const url = new URL(window.location.href);
            url.searchParams.set('status', $('#statusFilter').val());
            url.searchParams.set('type', $('#typeFilter').val());
            url.searchParams.set('currentPage', 1); // 重置到第一页
            window.location.href = url.toString();
        });

        // 状态筛选功能
        document.getElementById('statusFilter')?.addEventListener('change', () => {
            this.applyFilters();
        });

        // 类型筛选功能
        document.getElementById('typeFilter')?.addEventListener('change', () => {
            this.applyFilters();
        });

        // 初始化筛选器状态
        window.addEventListener('load', () => {
            const urlParams = new URLSearchParams(window.location.search);
            const status = urlParams.get('status');
            const type = urlParams.get('type');

            if (status) {
                document.getElementById('statusFilter').value = status;
            }
            if (type) {
                document.getElementById('typeFilter').value = type;
            }
        });
    }

    async handleUpload() {
        this.showLoading();
        try {
            // 表单验证
            const title = document.getElementById('title').value.trim();
            const description = document.getElementById('description').value.trim();
            const file = document.getElementById('file').files[0];
            const type = document.getElementById('type').value;
            const duration = document.getElementById('duration').value;
            const isRequired = document.getElementById('isRequired').checked;
            const passScore = type === 'QUIZ' ? document.getElementById('passScore').value : null;

            if (!title || !description || !file || !type) {
                throw new Error('请填写所有必填字段！');
            }

            if (type === 'QUIZ' && (!passScore || passScore < 0 || passScore > 100)) {
                throw new Error('测验类型必须设置0-100之间的通过分数！');
            }

            const formData = new FormData();
            formData.append('file', file);
            formData.append('creatorId', 1); // 这里应该是当前登录用户的ID
            formData.append('title', title);
            formData.append('description', description);
            formData.append('type', type);
            formData.append('duration', duration || '');
            formData.append('isRequired', isRequired);
            formData.append('tags', this.tagsManager.getTags().join(','));
            if (passScore) {
                formData.append('passScore', passScore);
            }

            const response = await fetch('/admin/training/uploadMaterial', {
                method: 'POST',
                headers: {
                    [this.csrfHeader]: this.csrfToken
                },
                body: formData
            });

            if (!response.ok) {
                const errorData = await response.json();
                throw new Error(errorData.message || `上传失败: ${response.status}`);
            }

            const data = await response.json();
            if (data.code === 200) {
                alert('上传成功');
                location.reload();
            } else {
                throw new Error(data.message || '上传失败');
            }
        } catch (error) {
            alert(error.message);
        } finally {
            this.hideLoading();
        }
    }

    async handleReview() {
        const materialId = this.reviewForm.dataset.materialId;
        const status = document.querySelector('input[name="status"]:checked').value;
        const comment = document.getElementById('reviewComment').value;

        try {
            const response = await fetch(`/admin/training/materials/${materialId}/status?newStatus=${status}&reviewComment=${encodeURIComponent(comment)}`, {
                method: 'PUT',
                headers: {
                    [this.csrfHeader]: this.csrfToken,
                    'Content-Type': 'application/json'
                }
            });

            if (response.ok) {
                alert('审核完成');
                location.reload();
            } else {
                const error = await response.text();
                alert('审核失败：' + error);
            }
        } catch (error) {
            alert('审核失败：' + error.message);
        }
    }

    async previewMaterial(materialId) {
        try {
            const response = await fetch(`/admin/training/materials/${materialId}`);
            if (response.ok) {
                let jsonData;
                try {
                    jsonData = await response.json();
                } catch (parseError) {
                    console.error('JSON解析错误:', parseError);
                    throw new Error('服务器返回的数据格式不正确');
                }

                if (jsonData.code === 200 && jsonData.data) {
                    const material = jsonData.data.material;
                    // 根据材料类型打开预览窗口
                    if (material && material.content) {
                        let previewUrl;
                        switch (material.type) {
                            case 'PDF':
                            case 'DOCUMENT':
                                previewUrl = `/preview/pdf?file=${encodeURIComponent(material.content)}`;
                                break;
                            case 'VIDEO':
                                previewUrl = `/preview/video?file=${encodeURIComponent(material.content)}`;
                                break;
                            default:
                                throw new Error('不支持的文件类型预览');
                        }
                        window.open(previewUrl, '_blank');
                    } else {
                        throw new Error('文件内容不存在');
                    }
                } else {
                    throw new Error(jsonData.message || '获取材料信息失败');
                }
            } else {
                const errorText = await response.text();
                throw new Error(errorText || '服务器响应错误');
            }
        } catch (error) {
            console.error('预览失败:', error);
            alert('预览失败：' + error.message);
        }
    }

    reviewMaterial(materialId) {
        this.reviewForm.dataset.materialId = materialId;
        this.reviewModal.style.display = 'block';
    }

    async editMaterial(materialId) {
        try {
            const response = await fetch(`/admin/training/materials/${materialId}`);
            if (response.ok) {
                const result = await response.json();
                if (result.code === 200 && result.data && result.data.material) {
                    const material = result.data.material;
                    // 显示编辑模态框
                    const editModal = document.getElementById('editModal');
                    editModal.style.display = 'block';

                    // 填充表单数据，添加默认值处理
                    document.getElementById('editTitle').value = material.title || '';
                    document.getElementById('editDescription').value = material.description || '';
                    document.getElementById('editType').value = material.type || 'DOCUMENT';
                    document.getElementById('editDuration').value = material.duration || '';
                    document.getElementById('editPassScore').value = material.passScore || '';
                    document.getElementById('editIsRequired').checked = Boolean(material.isRequired);

                    // 显示/隐藏通过分数输入框
                    const editPassScoreGroup = document.getElementById('editPassScoreGroup');
                    editPassScoreGroup.style.display = material.type === 'QUIZ' ? 'block' : 'none';

                    // 设置标签，确保tags存在且格式正确
                    const editTagsManager = new TagsManager('editTags', 'editTagsContainer');
                    editTagsManager.clearTags(); // 先清空现有标签
                    if (material && material.tags && typeof material.tags === 'string') {
                        const tags = material.tags.split(',').filter(tag => tag.trim());
                        tags.forEach(tag => editTagsManager.addTag(tag.trim()));
                    }

                    // 保存按钮点击事件
                    const submitEdit = document.getElementById('submitEdit');
                    submitEdit.onclick = async () => {
                        try {
                            const updatedMaterial = {
                                materialId: materialId,
                                title: document.getElementById('editTitle').value.trim(),
                                description: document.getElementById('editDescription').value.trim(),
                                type: document.getElementById('editType').value,
                                duration: parseInt(document.getElementById('editDuration').value) || null,
                                passScore: parseInt(document.getElementById('editPassScore').value) || null,
                                isRequired: document.getElementById('editIsRequired').checked,
                                tags: editTagsManager.getTags().join(','),
                                status: material && material.status ? material.status : 'PENDING' // 保持原有状态，如果不存在则默认为PENDING
                            };

                            // 表单验证
                            if (!updatedMaterial.title || !updatedMaterial.description) {
                                throw new Error('标题和描述为必填项！');
                            }

                            if (updatedMaterial.type === 'QUIZ' &&
                                (updatedMaterial.passScore === null ||
                                    updatedMaterial.passScore < 0 ||
                                    updatedMaterial.passScore > 100)) {
                                throw new Error('测验类型必须设置0-100之间的通过分数！');
                            }

                            const updateResponse = await fetch(`/admin/training/materials/${materialId}`, {
                                method: 'PUT',
                                headers: {
                                    'Content-Type': 'application/json',
                                    [this.csrfHeader]: this.csrfToken
                                },
                                body: JSON.stringify(updatedMaterial)
                            });

                            if (updateResponse.ok) {
                                alert('更新成功');
                                location.reload();
                            } else {
                                const error = await updateResponse.json();
                                throw new Error(error.message || '更新失败');
                            }
                        } catch (error) {
                            alert('更新失败：' + error.message);
                        }
                    };

                    // 关闭按钮事件
                    document.getElementById('cancelEdit').onclick = () => {
                        editModal.style.display = 'none';
                    };

                    // 类型变化时显示/隐藏通过分数输入框
                    document.getElementById('editType').onchange = (e) => {
                        editPassScoreGroup.style.display = e.target.value === 'QUIZ' ? 'block' : 'none';
                    };
                } else {
                    alert(result.message || '获取材料信息失败');
                }
            } else {
                alert('请求失败：' + response.status);
            }
        } catch (error) {
            alert('获取材料信息失败：' + error.message);
        }
    }

    async deleteMaterial(materialId) {
        if (!confirm('确定要删除这个培训材料吗？')) return;

        try {
            const response = await fetch(`/admin/training/materials/${materialId}`, {
                method: 'DELETE',
                headers: {
                    [this.csrfHeader]: this.csrfToken,
                    'Authorization': `Bearer ${localStorage.getItem('jwt_token')}`
                }
            });

            if (response.ok) {
                alert('删除成功');
                location.reload();
            } else {
                const error = await response.text();
                alert('删除失败：' + error);
            }
        } catch (error) {
            alert('删除失败：' + error.message);
        }
    }

    // 应用筛选条件
    applyFilters() {
        const status = document.getElementById('statusFilter').value;
        const type = document.getElementById('typeFilter').value;

        // 构建URL参数
        const params = new URLSearchParams();
        if (status) params.append('status', status);
        if (type) params.append('type', type);

        // 重定向到筛选后的URL
        window.location.href = `/admin/training${params.toString() ? '?' + params.toString() : ''}`;
    }

    // 初始化分配培训功能
    initAssignTrainingFeatures() {
        // 打开分配培训模态框
        document.getElementById('assignTrainingBtn')?.addEventListener('click', () => {
            $('#assignModal').show();
            // 初始化Select2
            $('#teacherSelect').select2({
                placeholder: '选择教师',
                allowClear: true,
                dropdownParent: $('#assignModal .modal-body'),
                width: '100%'
            });
            // 加载培训材料列表
            this.loadTrainingMaterials();
            // 直接加载教师列表
            this.loadTeachers();
        });

        // 关闭分配培训模态框
        document.querySelector('#assignModal .close')?.addEventListener('click', () => {
            $('#assignModal').hide();
        });
        document.getElementById('cancelAssign')?.addEventListener('click', () => {
            $('#assignModal').hide();
        });

        // 选择培训材料时显示详情
        document.getElementById('materialSelect')?.addEventListener('change', () => {
            const materialId = $('#materialSelect').val();
            if (materialId) {
                this.loadMaterialDetails(materialId);
            } else {
                $('#materialInfo').addClass('d-none');
            }
            this.updateAssignButtonState();
        });

        // 选择教师时更新按钮状态
        document.getElementById('teacherSelect')?.addEventListener('change', () => {
            this.updateAssignButtonState();
        });

        // 全选按钮
        document.getElementById('selectAllBtn')?.addEventListener('click', () => {
            $('#teacherSelect option').prop('selected', true);
            $('#teacherSelect').trigger('change');
        });

        // 清除选择按钮
        document.getElementById('clearSelectionBtn')?.addEventListener('click', () => {
            $('#teacherSelect').val(null).trigger('change');
        });

        // 分配按钮点击事件
        document.getElementById('submitAssign')?.addEventListener('click', () => {
            this.assignTraining();
        });
    }

    // 加载培训材料列表
    loadTrainingMaterials() {
        fetch('/admin/training/materials/list?status=PUBLISHED', {
            method: 'GET',
            headers: {
                [this.csrfHeader]: this.csrfToken
            }
        })
        .then(response => response.json())
        .then(response => {
            if (response.code === 200 && response.data) {
                const materials = Array.isArray(response.data) ? response.data : (response.data.content || []);
                let options = '<option value="">-- 请选择培训材料 --</option>';

                materials.forEach(function (material) {
                    options += `<option value="${material.materialId}">${material.title}</option>`;
                });

                document.getElementById('materialSelect').innerHTML = options;
                console.log('培训材料加载成功:', materials.length);
            } else {
                console.error('加载培训材料失败:', response);
                alert('加载培训材料失败：' + (response.message || '未知错误'));
            }
        })
        .catch(error => {
            console.error('加载培训材料请求失败:', error);
            alert('加载培训材料失败，请检查网络连接');
        });
    }

    // 加载教师列表
    loadTeachers() {
        const teacherSelect = document.getElementById('teacherSelect');
        teacherSelect.innerHTML = ''; // 清空现有选项

        fetch('/admin/training/assign/teachers?status=ACTIVE', {
            method: 'GET',
            headers: {
                [this.csrfHeader]: this.csrfToken
            }
        })
        .then(response => response.json())
        .then(response => {
            if (response.code === 200 && response.data) {
                const teachers = Array.isArray(response.data) ? response.data : (response.data.content || []);

                if (teachers.length > 0) {
                    teachers.forEach(function (teacher) {
                        const option = new Option(
                            `${teacher.name} (${teacher.department || '未知部门'})`,
                            teacher.teacherId,
                            false,
                            false
                        );
                        teacherSelect.appendChild(option);
                    });
                } else {
                    const option = new Option('没有可用的教师', '', true, true);
                    teacherSelect.appendChild(option);
                }

                // 如果使用了Select2插件，需要刷新
                if ($.fn.select2) {
                    $(teacherSelect).trigger('change');
                }
                
                console.log('教师列表加载成功:', teachers.length);
            } else {
                console.error('加载教师列表失败:', response);
                alert('加载教师列表失败：' + (response.message || '未知错误'));
            }
        })
        .catch(error => {
            console.error('加载教师列表请求失败:', error);
            alert('加载教师列表失败，请检查网络连接');
        });
    }

    // 加载培训材料详情
    loadMaterialDetails(materialId) {
        fetch(`/admin/training/materials/${materialId}`, {
            method: 'GET',
            headers: {
                [this.csrfHeader]: this.csrfToken
            }
        })
        .then(response => response.json())
        .then(response => {
            if (response.code === 200) {
                const material = response.data.material;

                document.getElementById('materialTitle').textContent = material.title;
                document.getElementById('materialDescription').textContent = material.description;
                document.getElementById('materialType').textContent = this.formatMaterialType(material.type);
                document.getElementById('materialDuration').textContent = material.duration || '未设置';
                document.getElementById('materialRequired').textContent = material.isRequired ? '是' : '否';
                document.getElementById('materialPassScore').textContent = material.passScore || '无';

                document.getElementById('materialInfo').style.display = 'block';
            } else {
                alert('加载培训材料详情失败：' + response.message);
            }
        })
        .catch(error => {
            console.error('加载培训材料详情失败:', error);
            alert('加载培训材料详情失败，请检查网络连接');
        });
    }



    // 更新分配按钮状态
    updateAssignButtonState() {
        const materialId = document.getElementById('materialSelect').value;
        const teacherSelect = document.getElementById('teacherSelect');
        const selectedTeachers = Array.from(teacherSelect.selectedOptions).map(option => option.value);

        const submitAssignBtn = document.getElementById('submitAssign');
        if (materialId && selectedTeachers && selectedTeachers.length > 0) {
            submitAssignBtn.disabled = false;
        } else {
            submitAssignBtn.disabled = true;
        }
    }

    // 分配培训
    assignTraining() {
        const materialId = document.getElementById('materialSelect').value;
        const teacherSelect = document.getElementById('teacherSelect');
        const teacherIds = Array.from(teacherSelect.selectedOptions).map(option => option.value);

        if (!materialId || !teacherIds || teacherIds.length === 0) {
            alert('请选择培训材料和教师');
            return;
        }

        // 显示加载状态
        const submitBtn = document.getElementById('submitAssign');
        submitBtn.disabled = true;
        submitBtn.innerHTML = '<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span> 分配中...';

        // 准备表单数据
        const formData = new FormData();
        formData.append('materialId', materialId);
        teacherIds.forEach(id => formData.append('teacherIds', id));

        // 添加CSRF令牌
        formData.append(this.csrfHeader, this.csrfToken);

        fetch('/admin/training/assign', {
            method: 'POST',
            headers: {
                [this.csrfHeader]: this.csrfToken
            },
            body: formData
        })
        .then(response => response.json())
        .then(response => {
            // 恢复按钮状态
            submitBtn.disabled = false;
            submitBtn.innerHTML = '<i class="fas fa-check-circle"></i> 确认分配';

            if (response.code === 200) {
                const result = response.data;

                // 显示结果
                document.getElementById('successCount').textContent = result.totalAssigned;
                document.getElementById('failCount').textContent = result.totalFailed;

                // 显示失败教师列表
                const failedTeachers = document.getElementById('failedTeachers');
                if (result.failedTeacherIds && result.failedTeacherIds.length > 0) {
                    let failedList = '';
                    result.failedTeacherIds.forEach(teacherId => {
                        const teacherOption = teacherSelect.querySelector(`option[value="${teacherId}"]`);
                        if (teacherOption) {
                            failedList += `<li>${teacherOption.textContent}</li>`;
                        } else {
                            failedList += `<li>教师ID: ${teacherId}</li>`;
                        }
                    });

                    document.getElementById('failedTeachersList').innerHTML = failedList;
                    failedTeachers.classList.remove('d-none');
                } else {
                    failedTeachers.classList.add('d-none');
                }

                // 显示结果区域
                document.getElementById('assignmentResult').style.display = 'block';

                // 如果全部成功，自动刷新教师列表
                if (result.totalFailed === 0) {
                    setTimeout(() => {
                        this.loadTeachers();
                    }, 1500);
                }
            } else {
                alert('分配培训失败：' + response.message);
            }
        })
        .catch(error => {
            // 恢复按钮状态
            submitBtn.disabled = false;
            submitBtn.innerHTML = '<i class="fas fa-check-circle"></i> 确认分配';
            alert('分配培训失败，请检查网络连接');
            console.error('分配培训错误:', error);
        });
    }

    // 格式化培训材料类型
    formatMaterialType(type) {
        const typeMap = {
            'VIDEO': '视频',
            'DOCUMENT': '文档',
            'QUIZ': '测验',
        };

        return typeMap[type] || type;
    }
}

// 初始化
const trainingManager = new TrainingManager();

//全局方法绑定
window.previewMaterial = (materialId) => trainingManager.previewMaterial(materialId);
window.editMaterial = (materialId) => trainingManager.editMaterial(materialId);
window.deleteMaterial = (materialId) => trainingManager.deleteMaterial(materialId);
window.reviewMaterial = (materialId) => trainingManager.reviewMaterial(materialId);