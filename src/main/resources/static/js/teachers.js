class TeachersManager {
    constructor() {
        this.currentPage = 1;
        this.totalPages = 1;
        this.pageSize = 10;

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

        this.init();
    }

    showLoading() {
        this.loadingDiv.style.display = 'flex';
    }

    hideLoading() {
        this.loadingDiv.style.display = 'none';
    }

    showError(message, type = 'error') {
        console.error(`[${new Date().toISOString()}] Error: ${message}`);

        // 显示友好的提示信息
        const notification = document.createElement('div');
        notification.className = `notification ${type}`;
        notification.innerHTML = `
            <span class="icon">${type === 'error' ? '⚠️' : 'ℹ️'}</span>
            <span class="message">${message}</span>
        `;

        document.body.appendChild(notification);

        // 自动消失
        setTimeout(() => {
            notification.classList.add('fade-out');
            setTimeout(() => notification.remove(), 500);
        }, 3000);
    }

    showSuccess(message) {
        const errorDiv = document.getElementById('errorMessage');
        errorDiv.textContent = message;
        errorDiv.classList.remove('error');
        errorDiv.classList.add('success');
        errorDiv.style.display = 'block';
        setTimeout(() => {
            errorDiv.textContent = '';
            errorDiv.style.display = 'none';
        }, 3000);
    }

    init() {
        this.initEventListeners();
        this.fetchTeachers();
    }

    changePageSize() {
        this.pageSize = parseInt(document.getElementById('pageSize').value, 10);
        this.currentPage = 1;
        this.fetchTeachers();
    }

    changePage(pageNumber) {
        if (pageNumber < 1 || pageNumber > this.totalPages) return;
        this.currentPage = pageNumber;
        this.fetchTeachers();
    }

    closeModal() {
        document.getElementById('teacherModal').style.display = 'none';
    }

    initEventListeners() {
        // 模态框相关事件
        document.getElementById('addTeacherBtn')?.addEventListener('click', () => this.showAddTeacherModal());
        document.querySelector('.close-button')?.addEventListener('click', () => this.closeModal());

        // 搜索功能
        document.getElementById('searchInput')?.addEventListener('input', (e) => {
            const keyword = e.target.value.trim();
            if (keyword.length >= 2) {
                this.searchTeachers(keyword);
            } else if (keyword.length === 0) {
                this.fetchTeachers();
            }
        });

        // 分页相关事件
        document.getElementById('firstPageBtn')?.addEventListener('click', () => {
            this.currentPage = 1;
            this.fetchTeachers();
        });
        document.getElementById('prevPageBtn')?.addEventListener('click', () => {
            this.currentPage = Math.max(1, this.currentPage - 1);
            this.fetchTeachers();
        });
        document.getElementById('nextPageBtn')?.addEventListener('click', () => {
            this.currentPage = Math.min(this.totalPages, this.currentPage + 1);
            this.fetchTeachers();
        });
        document.getElementById('lastPageBtn')?.addEventListener('click', () => {
            this.currentPage = this.totalPages;
            this.fetchTeachers();
        });
        document.getElementById('pageSize')?.addEventListener('change', () => {
            this.pageSize = parseInt(document.getElementById('pageSize').value, 10);
            this.currentPage = 1;
            this.fetchTeachers();
        });

        // 全选功能
        document.getElementById('selectAll')?.addEventListener('change', () => this.toggleSelectAll());

        // 批量操作事件
        document.getElementById('batchEnableBtn')?.addEventListener('click', () => this.batchUpdateStatus('ACTIVE'));
        document.getElementById('batchDisableBtn')?.addEventListener('click', () => this.batchUpdateStatus('DISABLED'));
        document.getElementById('batchDeleteBtn')?.addEventListener('click', () => this.batchDelete());

        // 表单提交
        document.getElementById('teacherForm')?.addEventListener('submit', (e) => this.saveTeacher(e));

        // 表单验证
        this.initFormValidation();

        // 动态绑定表格行按钮事件
        document.addEventListener('click', (e) => {
            if (e.target.closest('.edit-btn')) {
                const teacherId = e.target.closest('.edit-btn').dataset.teacherId;
                this.editTeacher(teacherId);
            }
            if (e.target.closest('.delete-btn')) {
                const teacherId = e.target.closest('.delete-btn').dataset.teacherId;
                this.deleteTeacher(teacherId);
            }
        });
    }

    async editTeacher(teacherId) {
        try {
            this.showLoading();
            const response = await fetch(`/api/admin/teachers/${teacherId}`, {
                headers: {
                    [this.csrfHeader]: this.csrfToken
                }
            });
            const data = await response.json();

            if (data.code === 200) {
                const teacher = data.data;
                // 确保teacherId被正确设置
                const teacherIdInput = document.getElementById('teacherId');
                if (teacherIdInput) {
                    teacherIdInput.value = teacher.teacherId || '';
                }
                document.getElementById('name').value = teacher.name || '';
                document.getElementById('department').value = teacher.department || '';
                document.getElementById('title').value = teacher.title || '';
                document.getElementById('email').value = teacher.email || '';
                document.getElementById('phone').value = teacher.phone || '';
                document.getElementById('status').value = teacher.status || 'ACTIVE';

                // 设置角色
                const selectedRole = teacher.roles?.[0]; // 获取第一个角色
                document.querySelectorAll('input[name="roles"]').forEach(radio => {
                    radio.checked = radio.value === selectedRole;
                });

                // 更新模态框标题
                document.getElementById('modalTitle').textContent = '编辑教师';
                document.getElementById('teacherModal').style.display = 'block';
            } else {
                throw new Error(data.message || '获取教师信息失败');
            }
        } catch (error) {
            this.showError(error.message);
        } finally {
            this.hideLoading();
        }
    }

    async deleteTeacher(teacherId) {
        if (!confirm('确定要删除这位教师吗？')) {
            return;
        }

        try {
            this.showLoading();
            const response = await fetch(`/api/admin/teachers/${teacherId}`, {
                method: 'DELETE',
                headers: {
                    [this.csrfHeader]: this.csrfToken
                }
            });
            const data = await response.json();

            if (data.code === 200) {
                this.showSuccess('删除教师成功');
                setTimeout(() => location.reload(), 1500);
            } else {
                throw new Error(data.message || '删除教师失败');
            }
        } catch (error) {
            this.showError(error.message);
        } finally {
            this.hideLoading();
        }
    }

    initFormValidation() {
        const form = document.getElementById('teacherForm');
        form.addEventListener('input', (e) => {
            const input = e.target;
            if (input.validity.valid) {
                input.classList.remove('invalid');
            } else {
                input.classList.add('invalid');
            }
        });
    }

    async fetchTeachers() {
        try {
            this.showLoading();
            const response = await fetch(`/api/admin/teachers/list?page=${this.currentPage - 1}&size=${this.pageSize}`, {
                headers: {
                    [this.csrfHeader]: this.csrfToken
                }
            });
            const data = await response.json();

            if (data.code === 200) {
                this.totalPages = Math.ceil(data.data.totalElements / this.pageSize);
                this.currentPage = Math.max(1, Math.min(this.currentPage, this.totalPages));
                this.renderTeachers(data.data.content);
                this.updatePaginationInfo(data.data);

                // 更新分页按钮状态
                this.updatePaginationButtons();
            } else {
                throw new Error(data.message || '获取教师数据失败');
            }
        } catch (error) {
            this.showError(error.message);
        } finally {
            this.hideLoading();
        }
    }

    async searchTeachers(keyword) {
        try {
            this.showLoading();
            const response = await fetch(`/api/admin/teachers/search?keyword=${encodeURIComponent(keyword)}`, {
                headers: {
                    [this.csrfHeader]: this.csrfToken
                }
            });
            const data = await response.json();

            if (data.code === 200) {
                this.renderTeachers(data.data);
                this.updatePaginationInfo({
                    totalElements: data.data.length,
                    totalPages: 1
                });
                this.updatePaginationButtons();
            } else {
                throw new Error(data.message || '搜索教师失败');
            }
        } catch (error) {
            this.showError(error.message);
        } finally {
            this.hideLoading();
        }
    }

    updatePaginationButtons() {
        const firstPageBtn = document.getElementById('firstPageBtn');
        const prevPageBtn = document.getElementById('prevPageBtn');
        const nextPageBtn = document.getElementById('nextPageBtn');
        const lastPageBtn = document.getElementById('lastPageBtn');

        firstPageBtn.disabled = this.currentPage === 1;
        prevPageBtn.disabled = this.currentPage === 1;
        nextPageBtn.disabled = this.currentPage === this.totalPages;
        lastPageBtn.disabled = this.currentPage === this.totalPages;
    }

    updatePaginationInfo(data) {
        const startRecord = (this.currentPage - 1) * this.pageSize + 1;
        const endRecord = Math.min(this.currentPage * this.pageSize, data.totalElements);

        document.getElementById('startRecord').textContent = startRecord;
        document.getElementById('endRecord').textContent = endRecord;
        document.getElementById('totalItems').textContent = data.totalElements;
        document.getElementById('currentPage').textContent = this.currentPage;
        document.getElementById('totalPages').textContent = this.totalPages;
    }

    async saveTeacher(event) {
        event.preventDefault();
        this.showLoading();

        try {
            const formData = new FormData(document.getElementById('teacherForm'));
            const teacherId = formData.get('teacherId');
            const selectedRole = document.querySelector('input[name="roles"]:checked')?.value;
            const selectedRoles = selectedRole ? [selectedRole] : [];

            // 调试信息
            console.log('Form Data:', {
                teacherId: formData.get('teacherId'),
                name: formData.get('name'),
                department: formData.get('department'),
                title: formData.get('title'),
                email: formData.get('email'),
                phone: formData.get('phone'),
                status: formData.get('status'),
                roles: selectedRoles
            });

            // 输入验证
            if (!this.validateTeacherForm(formData)) {
                console.log('Validation failed');
                return;
            }

            const teacher = {
                teacherId: teacherId ? parseInt(teacherId) : null,
                name: formData.get('name'),
                department: formData.get('department'),
                title: formData.get('title'),
                email: formData.get('email'),
                phone: formData.get('phone'),
                status: formData.get('status'),
                roles: selectedRoles,
                password: teacherId ? undefined : '123456' // 默认密码
            };

            let url, method;
            if (teacherId) {
                // 编辑模式
                url = `/api/admin/teachers/${teacherId}`;
                method = 'PUT';
                // 移除密码字段，编辑时不更新密码
                delete teacher.password;
            } else {
                // 添加模式
                url = '/api/admin/teachers';
                method = 'POST';
            }

            const response = await fetch(url, {
                method: method,
                headers: {
                    'Content-Type': 'application/json',
                    [this.csrfHeader]: this.csrfToken
                },
                body: JSON.stringify(teacher)
            });
            const data = await response.json();

            if (data.code === 200) {
                this.showSuccess(teacherId ? '教师信息更新成功' : '教师信息添加成功');
                this.fetchTeachers(); // 刷新表格数据
                this.closeModal();
            } else {
                throw new Error(data.message || '保存教师失败');
            }
        } catch (error) {
            this.showError(error.message);
        } finally {
            this.hideLoading();
        }
    }

    async batchUpdateStatus(newStatus) {
        const selectedIds = Array.from(document.querySelectorAll('.teacher-checkbox:checked'))
            .map(checkbox => parseInt(checkbox.value));

        if (selectedIds.length === 0) {
            this.showError('请先选择要操作的教师');
            return;
        }

        if (!confirm(`确定要将选中的${selectedIds.length}位教师状态更新为${newStatus === 'ACTIVE' ? '在职' : '停用'}吗？`)) {
            return;
        }

        this.showLoading();
        try {
            const response = await fetch('/api/admin/teachers/batch-update-status', {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                    [this.csrfHeader]: this.csrfToken
                },
                body: JSON.stringify({
                    ids: selectedIds,
                    status: newStatus
                })
            });

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            const data = await response.json();
            console.log('Batch update response:', data);

            if (data.code === 200) {
                this.showSuccess('批量更新状态成功');
                this.fetchTeachers();
            } else {
                throw new Error(data.message || '批量更新状态失败');
            }
        } catch (error) {
            console.error('Batch update error:', error);
            this.showError('系统错误：' + error.message);
        } finally {
            this.hideLoading();
        }
    }

    async batchDelete() {
        const selectedIds = Array.from(document.querySelectorAll('.teacher-checkbox:checked'))
            .map(checkbox => parseInt(checkbox.value));

        if (selectedIds.length === 0) {
            this.showError('请先选择要删除的教师');
            return;
        }

        if (!confirm(`确定要删除选中的${selectedIds.length}位教师吗？`)) {
            return;
        }

        this.showLoading();
        try {
            const response = await fetch('/api/admin/teachers/batch-delete', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    [this.csrfHeader]: this.csrfToken
                },
                body: JSON.stringify(selectedIds)
            });

            const data = await response.json();
            if (data.code === 200) {
                this.showSuccess('批量删除成功');
                setTimeout(() => location.reload(), 1500);
            } else {
                throw new Error(data.message || '批量删除失败');
            }
        } catch (error) {
            this.showError(error.message);
        } finally {
            this.hideLoading();
        }
    }

    validateTeacherForm(formData) {
        const name = formData.get('name');
        const email = formData.get('email');
        const phone = formData.get('phone');

        if (!name || typeof name !== 'string') {
            this.showError('姓名不能为空');
            return false;
        }

        const trimmedName = name.trim();
        if (trimmedName.length < 2 || trimmedName.length > 50) {
            this.showError('姓名长度应在2-50个字符之间（不包括前后空格）');
            return false;
        }

        if (!email) {
            this.showError('邮箱地址不能为空');
            return false;
        }
        if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
            this.showError('请输入有效的邮箱地址');
            return false;
        }

        if (!/^1[3-9]\d{9}$/.test(phone)) {
            this.showError('请输入有效的手机号码');
            return false;
        }

        return true;
    }

    showSuccess(message) {
        this.showError(message, 'success');
    }

    toggleSelectAll() {
        const checkboxes = document.querySelectorAll('.teacher-checkbox');
        const selectAll = document.getElementById('selectAll');
        checkboxes.forEach(checkbox => checkbox.checked = selectAll.checked);
    }

    showAddTeacherModal() {
        // 清空表单
        document.getElementById('teacherForm').reset();
        // 设置模态框标题
        document.getElementById('modalTitle').textContent = '添加教师';
        // 清空隐藏的teacherId
        document.getElementById('teacherId').value = '';
        // 重置角色选择
        document.querySelectorAll('input[name="roles"]').forEach(checkbox => {
            checkbox.checked = false;
        });
        // 显示模态框
        document.getElementById('teacherModal').style.display = 'block';
    }

    renderTeachers(teachers) {
        const tbody = document.getElementById('teachersTableBody');
        tbody.innerHTML = teachers.map(teacher => `
            <tr>
                <td><input type="checkbox" class="teacher-checkbox" value="${teacher.teacherId}"></td>
                <td>${teacher.teacherId}</td>
                <td>${teacher.name}</td>
                <td>${teacher.department}</td>
                <td>${teacher.title}</td>
                <td>${teacher.email}</td>
                <td>${teacher.phone}</td>
                <td>
                    <span class="status-badge ${teacher.status === 'ACTIVE' ? 'status-active' : 'status-inactive'}">
                        ${teacher.status === 'ACTIVE' ? '在职' : '离职'}
                    </span>
                </td>
                <td>
                    <div class="action-buttons">
                        <button class="btn btn-primary edit-btn" data-teacher-id="${teacher.teacherId}">
                            <i class="fas fa-edit"></i>
                        </button>
                        <button class="btn btn-danger delete-btn" data-teacher-id="${teacher.teacherId}">
                            <i class="fas fa-trash"></i>
                        </button>
                    </div>
                </td>
            </tr>
        `).join('');

        // 调整表格宽度
        this.adjustTableWidth();
    }

    adjustTableWidth() {
        const table = document.querySelector('.teachers-table');
        const container = document.querySelector('.table-container');
        if (table && container) {
            // 获取容器宽度
            const containerWidth = container.clientWidth;
            // 设置表格宽度
            table.style.width = `${containerWidth}px`;
        }
    }

    init() {
        this.initEventListeners();
        this.fetchTeachers();

        // 监听窗口大小变化
        window.addEventListener('resize', () => {
            this.adjustTableWidth();
        });
    }

    // 其他方法实现...
}

// 全局方法绑定
window.closeModal = () => teachersManager.closeModal();
window.saveTeacher = (e) => teachersManager.saveTeacher(e);

// 初始化教师管理器
const teachersManager = new TeachersManager();