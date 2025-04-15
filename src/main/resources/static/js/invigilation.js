/**
 * 监考管理系统前端JavaScript
 * 实现监考任务管理、监考确认流程、监考统计分析等功能
 * 包含监考记录查询、评价记录管理、异常情况记录和统计分析等模块
 */

document.addEventListener('DOMContentLoaded', function () {
    // 检查用户权限，决定加载哪种视图
    const isAdmin = document.querySelector('.tabs-container') !== null;

    // 初始化页面
    initPage(isAdmin);

    // 绑定事件监听器
    bindEventListeners(isAdmin);

    // 初始化标签页切换功能（仅管理员视图）
    if (isAdmin) {
        initTabs();
    }
});

/**
 * 初始化页面
 * @param {boolean} isAdmin - 是否为管理员视图
 */
function initPage(isAdmin) {
    if (isAdmin) {
        // 管理员视图初始化
        document.querySelector('.page-header h1').textContent = '监考管理';
        // 确保加载所有监考安排数据
        loadAllInvigilations();
    } else {
        // 教师视图初始化 - 在管理员界面不会执行此分支
        loadInvigilations();
    }
}

/**
 * 绑定事件监听器
 * @param {boolean} isAdmin - 是否为管理员视图
 */
function bindEventListeners(isAdmin) {
    // 筛选器事件绑定
    const statusFilter = document.querySelector('.filter-select');
    if (statusFilter) {
        statusFilter.addEventListener('change', function () {
            filterInvigilations(this.value);
        });
    }

    // 日期筛选器事件绑定
    const startDateFilter = document.getElementById('startDateFilter');
    const endDateFilter = document.getElementById('endDateFilter');
    if (startDateFilter && endDateFilter) {
        startDateFilter.addEventListener('change', function() {
            filterByDateRange(this.value, endDateFilter.value);
        });
        endDateFilter.addEventListener('change', function() {
            filterByDateRange(startDateFilter.value, this.value);
        });
    } else {
        // 向后兼容单个日期筛选
        const dateFilter = document.querySelector('.date-filter');
        if (dateFilter) {
            dateFilter.addEventListener('change', function() {
                filterByDate(this.value);
            });
        }
    }

    // 管理员特有的事件绑定
    if (isAdmin) {
        // 添加监考任务按钮
        const addButton = document.getElementById('addInvigilationBtn');
        if (addButton) {
            addButton.addEventListener('click', showAddInvigilationForm);
        }

        // 批量导入按钮
        const importButton = document.getElementById('importInvigilationsBtn');
        if (importButton) {
            importButton.addEventListener('click', showImportForm);
        }

        // 导出按钮
        const exportButton = document.getElementById('exportInvigilationsBtn');
        if (exportButton) {
            exportButton.addEventListener('click', exportInvigilations);
        }
    } else {
        // 教师特有的事件绑定
        bindTeacherEvents();
    }
}

/**
 * 绑定教师视图特有的事件
 */
function bindTeacherEvents() {
    // 可以在这里添加教师视图特有的事件绑定
    // 例如：请假申请、监考确认等
    const confirmButtons = document.querySelectorAll('.confirm-btn');
    confirmButtons.forEach(button => {
        button.addEventListener('click', function () {
            const invigilationId = this.dataset.id;
            confirmInvigilation(invigilationId);
        });
    });

    const rejectButtons = document.querySelectorAll('.reject-btn');
    rejectButtons.forEach(button => {
        button.addEventListener('click', function () {
            const invigilationId = this.dataset.id;
            rejectInvigilation(invigilationId);
        });
    });
}

/**
 * 获取认证头信息
 * @returns {Object} 包含认证信息的头部对象
 */
function getAuthHeaders() {
    // 获取CSRF令牌
    const csrfMeta = document.querySelector('meta[name="_csrf"]');
    const csrfHeaderMeta = document.querySelector('meta[name="_csrf_header"]');
    let headers = {
        'Content-Type': 'application/json'
    };

    if (csrfMeta && csrfHeaderMeta) {
        headers[csrfHeaderMeta.content] = csrfMeta.content;
    }

    // 获取JWT令牌
    const token = localStorage.getItem('token');
    if (token) {
        headers['Authorization'] = 'Bearer ' + token;
    }

    return headers;
}

/**
 * 加载所有监考任务（管理员视图）
 */
function loadAllInvigilations() {
    const grid = document.getElementById('invigilationGrid');
    grid.innerHTML = '<div class="loading">加载中...</div>';

    // 获取日期筛选值
    const startDate = document.getElementById('startDateFilter')?.value;
    const endDate = document.getElementById('endDateFilter')?.value;

    // 构建API URL，添加日期范围参数
    let apiUrl = '/api/admin/invigilation/assignments/all';
    const params = new URLSearchParams();
    if (startDate) params.append('startTime', `${startDate}T00:00:00`);
    if (endDate) params.append('endTime', `${endDate}T23:59:59`);
    if (params.toString()) {
        apiUrl += '?' + params.toString();
    }

    // 添加一个表格来显示所有考试信息
    const assignmentsTab = document.getElementById('assignments');
    if (assignmentsTab && !document.getElementById('examsTable')) {
        const tableContainer = document.createElement('div');
        tableContainer.className = 'exams-table-container';
        tableContainer.innerHTML = `
            <h3 class="section-title">所有考试安排</h3>
            <table id="examsTable" class="data-table">
                <thead>
                    <tr>
                        <th>课程名称</th>
                        <th>考试日期</th>
                        <th>考试时间</th>
                        <th>考试地点</th>
                        <th>监考教师</th>
                        <th>监考角色</th>
                        <th>状态</th>
                        <th>操作</th>
                    </tr>
                </thead>
                <tbody id="examsTableBody">
                    <!-- 考试数据将通过JavaScript动态加载 -->
                </tbody>
            </table>
        `;
        assignmentsTab.appendChild(tableContainer);
    }

    fetch(apiUrl, {
        headers: getAuthHeaders()
    })
        .then(response => {
            if (!response.ok) {
                throw new Error(response.statusText);
            }
            return response.json();
        })
        .then(response => {
            if (response.code === 200 && Array.isArray(response.data)) {
                grid.innerHTML = '';
                if (response.data.length === 0) {
                    grid.innerHTML = '<div class="no-data">暂无监考安排</div>';
                    return;
                }
                response.data.forEach(invigilation => {
                    grid.appendChild(createInvigilationCard(invigilation, true));
                });

                // 更新表格数据
                updateExamsTable(response.data);

                // 添加统计信息
                updateStatistics(response.data);
            } else {
                throw new Error(response.message || '加载失败');
            }
        })
        .catch(error => {
            console.error('加载监考任务失败:', error);
            // 检查是否是认证错误
            if (error.message.includes('Unauthorized') || error.message.includes('Forbidden')) {
                grid.innerHTML = `<div class="error-message">认证失败，请<a href="/login">重新登录</a></div>`;
            } else {
                grid.innerHTML = `<div class="error-message">加载失败: ${error.message}<br><button onclick="loadAllInvigilations()">重试</button></div>`;
            }
        });
}

/**
 * 更新考试表格
 * @param {Array} invigilations - 监考任务数据数组
 */
function updateExamsTable(invigilations) {
    const tableBody = document.getElementById('examsTableBody');
    if (!tableBody) return;

    tableBody.innerHTML = '';

    invigilations.forEach(invigilation => {
        const row = document.createElement('tr');

        // 格式化日期和时间，并处理可能的无效日期
        let formattedDate = 'N/A';
        let startTime = 'N/A';
        let endTime = 'N/A';

        try {
            if (invigilation.examStart && !isNaN(new Date(invigilation.examStart).getTime())) {
                const examDate = new Date(invigilation.examStart);
                formattedDate = `${examDate.getFullYear()}-${(examDate.getMonth() + 1).toString().padStart(2, '0')}-${examDate.getDate().toString().padStart(2, '0')}`;
                startTime = examDate.toTimeString().substring(0, 5);
            }

            if (invigilation.examEnd && !isNaN(new Date(invigilation.examEnd).getTime())) {
                endTime = new Date(invigilation.examEnd).toTimeString().substring(0, 5);
            }
        } catch (e) {
            console.error('日期格式化错误:', e);
        }

        // 状态文本
        let statusText = '';
        switch ((invigilation.status || '').toLowerCase()) {
            case 'pending': statusText = '待确认'; break;
            case 'confirmed': statusText = '已确认'; break;
            case 'completed': statusText = '已完成'; break;
            case 'cancelled': statusText = '已取消'; break;
            default: statusText = invigilation.status || '未知';
        }

        // 获取教师姓名 - 这里应该通过API获取或使用已有数据
        // 目前暂时使用teacherName字段，如果不存在则显示未分配
        const teacherName = invigilation.teacherName || '未分配';

        row.innerHTML = `
            <td>${invigilation.courseName || '未知课程'}</td>
            <td>${formattedDate}</td>
            <td>${startTime} - ${endTime}</td>
            <td>${invigilation.location || 'N/A'}</td>
            <td>${teacherName}</td>
            <td>${invigilation.role || '-'}</td>
            <td><span class="status-badge status-${(invigilation.status || 'pending').toLowerCase()}">${statusText}</span></td>
            <td>
                <button class="btn small-btn edit-btn" data-id="${invigilation.assignmentId || ''}">编辑</button>
                <button class="btn small-btn details-btn" data-id="${invigilation.assignmentId || ''}">详情</button>
            </td>
        `;

        tableBody.appendChild(row);
    });

    // 绑定按钮事件
    document.querySelectorAll('#examsTableBody .edit-btn').forEach(btn => {
        btn.addEventListener('click', function () {
            showEditInvigilationForm(this.dataset.id);
        });
    });

    document.querySelectorAll('#examsTableBody .details-btn').forEach(btn => {
        btn.addEventListener('click', function () {
            showInvigilationDetails(this.dataset.id);
        });
    });
}

/**
 * 加载个人监考任务（教师视图）
 */
function loadInvigilations() {
    const grid = document.getElementById('invigilationGrid');
    grid.innerHTML = '<div class="loading">加载中...</div>';

    // 由于这是管理员界面，我们应该使用管理员API获取所有监考安排
    // 而不是尝试获取个人监考安排
    fetch('/api/assignments/my', {
        headers: getAuthHeaders()
    })
        .then(response => {
            if (!response.ok) {
                throw new Error(response.statusText);
            }
            return response.json();
        })
        .then(response => {
            if (response.code === 200 && Array.isArray(response.data)) {
                grid.innerHTML = '';
                if (response.data.length === 0) {
                    grid.innerHTML = '<div class="no-data">暂无监考安排</div>';
                    return;
                }
                response.data.forEach(invigilation => {
                    grid.appendChild(createInvigilationCard(invigilation, false));
                });
            } else {
                throw new Error(response.message || '加载失败');
            }
        })
        .catch(error => {
            console.error('加载监考任务失败:', error);
            // 检查是否是认证错误
            if (error.message.includes('Unauthorized') || error.message.includes('Forbidden')) {
                grid.innerHTML = `<div class="error-message">认证失败，请<a href="/login">重新登录</a></div>`;
            } else {
                grid.innerHTML = `<div class="error-message">加载失败: ${error.message}<br><button onclick="loadInvigilations()">重试</button></div>`;
            }
        });
}

/**
 * 创建监考任务卡片
 * @param {Object} invigilation - 监考任务数据
 * @param {boolean} isAdmin - 是否为管理员视图
 * @returns {HTMLElement} - 监考任务卡片元素
 */
function createInvigilationCard(invigilation, isAdmin) {
    const card = document.createElement('div');
    card.className = `invigilation-card status-${(invigilation.status || 'pending').toLowerCase()}`;
    card.dataset.id = invigilation.assignmentId || '';

    // 构建卡片内容并处理可能的无效日期
    let formattedDate = 'N/A';
    let startTime = 'N/A';
    let endTime = 'N/A';
    
    try {
        // 处理examStart作为日期来源
        if (invigilation.examStart && !isNaN(new Date(invigilation.examStart).getTime())) {
            const examDate = new Date(invigilation.examStart);
            formattedDate = `${examDate.getFullYear()}-${(examDate.getMonth() + 1).toString().padStart(2, '0')}-${examDate.getDate().toString().padStart(2, '0')}`;
            startTime = examDate.toTimeString().substring(0, 5);
            
            // 如果有examEnd，获取结束时间
            if (invigilation.examEnd && !isNaN(new Date(invigilation.examEnd).getTime())) {
                endTime = new Date(invigilation.examEnd).toTimeString().substring(0, 5);
            }
        }
        // 兼容旧格式：如果没有examStart但有examDate
        else if (invigilation.examDate && !isNaN(new Date(invigilation.examDate).getTime())) {
            const examDate = new Date(invigilation.examDate);
            formattedDate = `${examDate.getFullYear()}-${(examDate.getMonth() + 1).toString().padStart(2, '0')}-${examDate.getDate().toString().padStart(2, '0')}`;
            
            // 使用examStartTime和examEndTime（如果存在）
            startTime = invigilation.examStartTime || 'N/A';
            endTime = invigilation.examEndTime || 'N/A';
        }
    } catch (e) {
        console.error('日期格式化错误:', e);
    }

    // 处理可能为undefined的地点
    const location = invigilation.location || 'N/A';

    // 获取教师姓名 - 优先使用teacherName字段，如果不存在则显示未分配
    const teacherName = invigilation.teacherName || '未分配';

    // 状态标签
    let statusText = '';
    switch ((invigilation.status || '').toLowerCase()) {
        case 'pending': statusText = '待确认'; break;
        case 'confirmed': statusText = '已确认'; break;
        case 'completed': statusText = '已完成'; break;
        case 'cancelled': statusText = '已取消'; break;
        default: statusText = invigilation.status || '未知';
    }

    card.innerHTML = `
        <div class="card-header">
            <h3>${invigilation.courseName || '未知课程'}</h3>
            <span class="status-badge">${statusText}</span>
        </div>
        <div class="card-body">
            <p><strong>考试日期:</strong> ${formattedDate}</p>
            <p><strong>考试时间:</strong> ${startTime} - ${endTime}</p>
            <p><strong>考试地点:</strong> ${location}</p>
            <p><strong>监考教师:</strong> ${teacherName}</p>
            <p><strong>监考角色:</strong> ${invigilation.role || '未指定'}</p>
        </div>
        <div class="card-footer">
            <button class="btn details-btn" data-id="${invigilation.assignmentId || ''}">详情</button>
            ${getActionButtons(invigilation, isAdmin)}
        </div>
    `;

    // 绑定详情按钮事件
    card.querySelector('.details-btn').addEventListener('click', function () {
        showInvigilationDetails(invigilation.assignmentId);
    });

    // 绑定其他按钮事件
    bindCardButtonEvents(card, invigilation, isAdmin);

    return card;
}

/**
 * 获取操作按钮HTML
 * @param {Object} invigilation - 监考任务数据
 * @param {boolean} isAdmin - 是否为管理员视图
 * @returns {string} - 按钮HTML字符串
 */
function getActionButtons(invigilation, isAdmin) {
    let buttons = '';
    const id = invigilation.assignmentId || '';
    const status = (invigilation.status || 'pending').toLowerCase();

    // 根据状态和用户角色显示不同的按钮
    if (isAdmin) {
        // 管理员视图按钮
        buttons += `<button class="btn edit-btn" data-id="${id}">编辑</button>`;

        if (status !== 'cancelled') {
            buttons += `<button class="btn cancel-btn" data-id="${id}">取消</button>`;
        }

        if (status === 'confirmed') {
            buttons += `<button class="btn complete-btn" data-id="${id}">标记完成</button>`;
        }
    } else {
        // 教师视图按钮
        if (status === 'pending') {
            buttons += `
                <button class="btn confirm-btn" data-id="${id}">确认</button>
                <button class="btn reject-btn" data-id="${id}">拒绝</button>
            `;
        }

        if (status === 'confirmed') {
            buttons += `<button class="btn leave-btn" data-id="${id}">请假</button>`;
        }
    }

    return buttons;
}

/**
 * 绑定卡片按钮事件
 * @param {HTMLElement} card - 卡片元素
 * @param {Object} invigilation - 监考任务数据
 * @param {boolean} isAdmin - 是否为管理员视图
 */
function bindCardButtonEvents(card, invigilation, isAdmin) {
    const id = invigilation.assignmentId || '';
    
    // 编辑按钮（仅管理员）
    const editBtn = card.querySelector('.edit-btn');
    if (editBtn) {
        editBtn.addEventListener('click', function () {
            showEditInvigilationForm(id);
        });
    }

    // 取消按钮（仅管理员）
    const cancelBtn = card.querySelector('.cancel-btn');
    if (cancelBtn) {
        cancelBtn.addEventListener('click', function () {
            cancelInvigilation(id);
        });
    }

    // 标记完成按钮（仅管理员）
    const completeBtn = card.querySelector('.complete-btn');
    if (completeBtn) {
        completeBtn.addEventListener('click', function () {
            completeInvigilation(id);
        });
    }

    // 确认按钮（仅教师）
    const confirmBtn = card.querySelector('.confirm-btn');
    if (confirmBtn) {
        confirmBtn.addEventListener('click', function () {
            confirmInvigilation(id);
        });
    }

    // 拒绝按钮（仅教师）
    const rejectBtn = card.querySelector('.reject-btn');
    if (rejectBtn) {
        rejectBtn.addEventListener('click', function () {
            rejectInvigilation(id);
        });
    }

    // 请假按钮（仅教师）
    const leaveBtn = card.querySelector('.leave-btn');
    if (leaveBtn) {
        leaveBtn.addEventListener('click', function () {
            showLeaveForm(id);
        });
    }
}

/**
 * 按状态筛选监考任务
 * @param {string} status - 监考状态
 */
function filterInvigilations(status) {
    const cards = document.querySelectorAll('.invigilation-card');
    cards.forEach(card => {
        if (status === 'all' || card.classList.contains(`status-${status.toLowerCase()}`)) {
            card.style.display = 'block';
        } else {
            card.style.display = 'none';
        }
    });
}

/**
 * 按日期范围筛选监考任务
 * @param {string} startDate - 开始日期字符串
 * @param {string} endDate - 结束日期字符串
 */
function filterByDateRange(startDate, endDate) {
    const cards = document.querySelectorAll('.invigilation-card');
    cards.forEach(card => {
        const cardDateElement = card.querySelector('p:nth-child(1)');
        if (!cardDateElement) return;
        
        const cardDateText = cardDateElement.textContent.split(':')[1].trim();
        if (!cardDateText) return;
        
        // 将卡片日期转换为Date对象进行比较
        const cardDate = new Date(cardDateText);
        
        // 如果日期无效，则显示卡片
        if (isNaN(cardDate.getTime())) {
            card.style.display = 'block';
            return;
        }
        
        // 日期比较逻辑
        let showCard = true;
        if (startDate && !isNaN(new Date(startDate).getTime())) {
            const start = new Date(startDate);
            showCard = showCard && cardDate >= start;
        }
        
        if (endDate && !isNaN(new Date(endDate).getTime())) {
            const end = new Date(endDate);
            // 设置结束日期为当天的23:59:59，以包含整天
            end.setHours(23, 59, 59, 999);
            showCard = showCard && cardDate <= end;
        }
        
        card.style.display = showCard ? 'block' : 'none';
    });
}

/**
 * 按日期筛选监考任务 (保留向后兼容)
 * @param {string} date - 日期字符串
 */
function filterByDate(date) {
    // 调用新的日期范围筛选函数，只传入开始日期
    filterByDateRange(date, null);
}

/**
 * 更新统计信息
 * @param {Array} invigilations - 监考任务数据数组
 */
function updateStatistics(invigilations) {
    const stats = {
        total: invigilations.length,
        pending: 0,
        confirmed: 0,
        completed: 0
    };

    invigilations.forEach(invigilation => {
        switch (invigilation.status.toLowerCase()) {
            case 'pending':
                stats.pending++;
                break;
            case 'confirmed':
                stats.confirmed++;
                break;
            case 'completed':
                stats.completed++;
                break;
        }
    });

    // 更新统计面板
    document.getElementById('totalInvigilations').textContent = stats.total;
    document.getElementById('pendingInvigilations').textContent = stats.pending;
    document.getElementById('confirmedInvigilations').textContent = stats.confirmed;
    document.getElementById('completedInvigilations').textContent = stats.completed;
}

/**
 * 显示添加监考任务表单
 */
function showAddInvigilationForm() {
    const modal = document.createElement('div');
    modal.className = 'modal';
    modal.innerHTML = `
        <div class="modal-content">
            <h2>添加监考任务</h2>
            <form id="addInvigilationForm">
                <div class="form-group">
                    <label>课程名称：</label>
                    <input type="text" name="courseName" required>
                </div>
                <div class="form-group">
                    <label>考试时间：</label>
                    <input type="datetime-local" name="examDate" required>
                </div>
                <div class="form-group">
                    <label>考试地点：</label>
                    <input type="text" name="location" required>
                </div>
                <div class="form-group">
                    <label>监考教师：</label>
                    <input type="text" name="teacherName" required>
                </div>
                <div class="form-group">
                    <label>备注：</label>
                    <textarea name="remarks"></textarea>
                </div>
                <div class="form-actions">
                    <button type="submit" class="btn primary-btn">提交</button>
                    <button type="button" class="btn secondary-btn" onclick="closeModal(this)">取消</button>
                </div>
            </form>
        </div>
    `;
    document.body.appendChild(modal);

    // 绑定表单提交事件
    document.getElementById('addInvigilationForm').addEventListener('submit', function (e) {
        e.preventDefault();
        submitInvigilationForm(this);
    });
}

/**
 * 提交监考任务表单
 * @param {HTMLFormElement} form - 表单元素
 */
function submitInvigilationForm(form) {
    const formData = new FormData(form);
    const data = Object.fromEntries(formData.entries());

    fetch('/api/admin/invigilation/assignments/add', {
        method: 'POST',
        headers: {
            ...auth.getAuthHeader(),
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(data)
    })
        .then(response => response.json())
        .then(response => {
            if (response.code === 200) {
                showMessage('添加成功', 'success');
                closeModal(form);
                loadAllInvigilations(); // 重新加载监考任务列表
            } else {
                throw new Error(response.message || '添加失败');
            }
        })
        .catch(error => {
            showMessage(error.message, 'error');
        });
}

/**
 * 显示批量导入表单
 */
function showImportForm() {
    const modal = document.createElement('div');
    modal.className = 'modal';
    modal.innerHTML = `
        <div class="modal-content">
            <h2>批量导入监考任务</h2>
            <form id="importForm">
                <div class="form-group">
                    <label>选择Excel文件：</label>
                    <input type="file" name="file" accept=".xlsx,.xls" required>
                </div>
                <div class="form-actions">
                    <button type="submit" class="btn primary-btn">导入</button>
                    <button type="button" class="btn secondary-btn" onclick="closeModal(this)">取消</button>
                </div>
            </form>
        </div>
    `;
    document.body.appendChild(modal);

    // 绑定表单提交事件
    document.getElementById('importForm').addEventListener('submit', function (e) {
        e.preventDefault();
        importInvigilations(this);
    });
}

/**
 * 导入监考任务
 * @param {HTMLFormElement} form - 表单元素
 */
function importInvigilations(form) {
    const formData = new FormData(form);

    fetch('/api/admin/invigilation/assignments/import', {
        method: 'POST',
        headers: {
            ...auth.getAuthHeader()
        },
        body: formData
    })
        .then(response => response.json())
        .then(response => {
            if (response.code === 200) {
                showMessage('导入成功', 'success');
                closeModal(form);
                loadAllInvigilations(); // 重新加载监考任务列表
            } else {
                throw new Error(response.message || '导入失败');
            }
        })
        .catch(error => {
            showMessage(error.message, 'error');
        });
}

/**
 * 导出监考安排
 */
function exportInvigilations() {
    fetch('/api/admin/invigilation/assignments/export', {
        headers: auth.getAuthHeader()
    })
        .then(response => response.blob())
        .then(blob => {
            const url = window.URL.createObjectURL(blob);
            const a = document.createElement('a');
            a.href = url;
            a.download = `监考安排_${new Date().toLocaleDateString()}.xlsx`;
            document.body.appendChild(a);
            a.click();
            window.URL.revokeObjectURL(url);
            a.remove();
        })
        .catch(error => {
            showMessage('导出失败：' + error.message, 'error');
        });
}

/**
 * 关闭模态框
 * @param {HTMLElement} element - 模态框内的元素
 */
function closeModal(element) {
    const modal = element.closest('.modal');
    if (modal) {
        modal.remove();
    }
}

/**
 * 显示消息提示
 * @param {string} message - 消息内容
 * @param {string} type - 消息类型（success/error）
 */
function showMessage(message, type) {
    const toast = document.createElement('div');
    toast.className = `toast ${type}`;
    toast.textContent = message;
    document.body.appendChild(toast);

    setTimeout(() => {
        toast.remove();
    }, 3000);
}

/**
 * 显示消息提示
 * @param {string} message - 消息内容
 * @param {string} type - 消息类型（success/error）
 */
function showMessage(message, type) {
    const toast = document.createElement('div');
    toast.className = `toast ${type}`;
    toast.textContent = message;
    document.body.appendChild(toast);

    setTimeout(() => {
        toast.remove();
    }, 3000);
}

/**
 * 显示消息提示
 * @param {string} message - 消息内容
 * @param {string} type - 消息类型（success/error）
 */
function showMessage(message, type) {
    const toast = document.createElement('div');
    toast.className = `toast ${type}`;
    toast.textContent = message;
    document.body.appendChild(toast);

    setTimeout(() => {
        toast.remove();
    }, 3000);
}