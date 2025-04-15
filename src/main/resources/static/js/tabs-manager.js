/**
 * 标签页管理模块
 * 实现监考管理系统中的标签页切换功能
 * 包含监考任务、监考记录、评价管理、异常情况和统计分析等标签页
 */

/**
 * 初始化标签页切换功能
 */
function initTabs() {
    const tabButtons = document.querySelectorAll('.tab-btn');
    const tabContents = document.querySelectorAll('.tab-content');

    // 为每个标签按钮添加点击事件
    tabButtons.forEach(button => {
        button.addEventListener('click', function() {
            // 移除所有标签按钮的active类
            tabButtons.forEach(btn => btn.classList.remove('active'));
            // 移除所有内容区域的active类
            tabContents.forEach(content => content.classList.remove('active'));
            
            // 为当前点击的按钮添加active类
            this.classList.add('active');
            
            // 获取对应的内容区域并添加active类
            const tabId = this.dataset.tab;
            const tabContent = document.getElementById(tabId);
            if (tabContent) {
                tabContent.classList.add('active');
                
                // 根据不同的标签页加载相应的数据
                loadTabContent(tabId);
            }
        });
    });
}

/**
 * 根据标签ID加载对应的内容
 * @param {string} tabId - 标签ID
 */
function loadTabContent(tabId) {
    switch(tabId) {
        case 'assignments':
            // 监考任务标签页 - 已在页面加载时通过loadAllInvigilations()加载
            break;
        case 'records':
            // 监考记录标签页
            loadInvigilationRecords();
            break;
        case 'evaluations':
            // 评价管理标签页
            loadEvaluations();
            break;
        case 'exceptions':
            // 异常情况标签页
            loadExceptions();
            break;
        case 'statistics':
            // 统计分析标签页
            loadStatistics();
            break;
    }
}

/**
 * 加载监考记录数据
 */
function loadInvigilationRecords() {
    const recordsGrid = document.getElementById('recordsGrid');
    if (!recordsGrid) return;
    
    recordsGrid.innerHTML = '<div class="loading">加载中...</div>';
    
    // 获取筛选条件
    const searchInput = document.getElementById('recordSearchInput');
    const typeFilter = document.getElementById('recordTypeFilter');
    const searchText = searchInput ? searchInput.value : '';
    const recordType = typeFilter ? typeFilter.value : 'all';
    
    // 调用API获取监考记录数据
    fetch(`/api/admin/invigilation/records?search=${searchText}&type=${recordType}`, {
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
            displayInvigilationRecords(response.data, recordsGrid);
        } else {
            throw new Error(response.message || '加载失败');
        }
    })
    .catch(error => {
        recordsGrid.innerHTML = `<div class="error-message">加载失败: ${error.message}<br><button onclick="loadInvigilationRecords()">重试</button></div>`;
    });
    
    // 绑定搜索按钮事件
    const searchBtn = document.getElementById('searchRecordsBtn');
    if (searchBtn) {
        searchBtn.addEventListener('click', loadInvigilationRecords);
    }
}

/**
 * 显示监考记录数据
 * @param {Array} records - 监考记录数据
 * @param {HTMLElement} container - 容器元素
 */
function displayInvigilationRecords(records, container) {
    if (records.length === 0) {
        container.innerHTML = '<div class="no-data">暂无监考记录</div>';
        return;
    }
    
    // 创建表格显示监考记录
    const table = document.createElement('table');
    table.className = 'data-table';
    table.innerHTML = `
        <thead>
            <tr>
                <th>监考ID</th>
                <th>监考名称</th>
                <th>监考教师</th>
                <th>监考时间</th>
                <th>记录类型</th>
                <th>记录内容</th>
                <th>操作</th>
            </tr>
        </thead>
        <tbody>
            ${records.map(record => {
                // 处理记录类型
                let recordTypeText = '正常';
                let isException = false;
                
                if (record.type) {
                    // 处理枚举类型
                    if (record.type === 'INCIDENT' || record.type === 'VIOLATION') {
                        recordTypeText = '异常';
                        isException = true;
                    } else if (record.type === 'SIGN_IN') {
                        recordTypeText = '签到';
                    } else if (record.type === 'NOTE') {
                        recordTypeText = '备注';
                    } else if (typeof record.type === 'object' && record.type.value) {
                        // 处理对象形式的枚举
                        if (record.type.value === 'INCIDENT' || record.type.value === 'VIOLATION') {
                            recordTypeText = '异常';
                            isException = true;
                        } else if (record.type.value === 'SIGN_IN') {
                            recordTypeText = '签到';
                        } else if (record.type.value === 'NOTE') {
                            recordTypeText = '备注';
                        }
                    }
                }
                
                // 处理监考时间
                let timeDisplay = '-';
                if (record.createTime) {
                    timeDisplay = formatDateTime(record.createTime);
                } else if (record.submitTime) {
                    timeDisplay = formatDateTime(record.submitTime);
                }
                
                return `
                <tr class="${isException ? 'exception-row' : ''}">
                    <td>${record.assignmentId || '-'}</td>
                    <td>${record.courseName || '未知监考'}</td>
                    <td>${record.teacherName || '-'}</td>
                    <td>${timeDisplay}</td>
                    <td>${recordTypeText}</td>
                    <td>${record.content || '-'}</td>
                    <td>
                        <button class="btn small-btn details-btn" data-id="${record.recordId}">详情</button>
                    </td>
                </tr>
            `}).join('')}
        </tbody>
    `;
    
    container.innerHTML = '';
    container.appendChild(table);
    
    // 绑定详情按钮事件
    container.querySelectorAll('.details-btn').forEach(btn => {
        btn.addEventListener('click', function() {
            showRecordDetails(this.dataset.id);
        });
    });
}

/**
 * 加载评价管理数据
 */
function loadEvaluations() {
    const evaluationsContainer = document.querySelector('#evaluations .evaluations-grid');
    if (!evaluationsContainer) return;
    
    evaluationsContainer.innerHTML = '<div class="loading">加载中...</div>';
    
    // 获取筛选条件
    const teacherSearch = document.getElementById('teacherSearchInput');
    const ratingFilter = document.getElementById('ratingFilter');
    const searchText = teacherSearch ? teacherSearch.value : '';
    const rating = ratingFilter ? ratingFilter.value : 'all';
    
    // 调用API获取评价数据
    fetch(`/api/admin/invigilation/evaluations?teacher=${searchText}&rating=${rating}`, {
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
            displayEvaluations(response.data, evaluationsContainer);
        } else {
            throw new Error(response.message || '加载失败');
        }
    })
    .catch(error => {
        evaluationsContainer.innerHTML = `<div class="error-message">加载失败: ${error.message}<br><button onclick="loadEvaluations()">重试</button></div>`;
    });
}

/**
 * 显示评价数据
 * @param {Array} evaluations - 评价数据
 * @param {HTMLElement} container - 容器元素
 */
function displayEvaluations(evaluations, container) {
    if (evaluations.length === 0) {
        container.innerHTML = '<div class="no-data">暂无评价数据</div>';
        return;
    }
    
    // 创建评价卡片
    container.innerHTML = '';
    evaluations.forEach(evaluation => {
        const card = document.createElement('div');
        card.className = 'evaluation-card';
        card.innerHTML = `
            <div class="evaluation-header">
                <h3>${evaluation.teacherName || '未知教师'}</h3>
                <div class="rating">
                    ${generateStars(evaluation.rating)}
                </div>
            </div>
            <div class="evaluation-body">
                <p><strong>课程:</strong> ${evaluation.courseName || '未知课程'}</p>
                <p><strong>评价时间:</strong> ${formatDateTime(evaluation.evaluationTime)}</p>
                <p><strong>评价内容:</strong> ${evaluation.content || '无评价内容'}</p>
            </div>
            <div class="evaluation-footer">
                <button class="btn small-btn" data-id="${evaluation.evaluationId}">查看详情</button>
            </div>
        `;
        container.appendChild(card);
    });
    
    // 绑定详情按钮事件
    container.querySelectorAll('.evaluation-footer .btn').forEach(btn => {
        btn.addEventListener('click', function() {
            showEvaluationDetails(this.dataset.id);
        });
    });
}

/**
 * 生成星级评分HTML
 * @param {number} rating - 评分（1-5）
 * @returns {string} - 星级HTML
 */
function generateStars(rating) {
    rating = parseInt(rating) || 0;
    let stars = '';
    for (let i = 1; i <= 5; i++) {
        if (i <= rating) {
            stars += '<i class="fas fa-star"></i>';
        } else {
            stars += '<i class="far fa-star"></i>';
        }
    }
    return stars;
}

/**
 * 加载异常情况数据
 */
function loadExceptions() {
    const exceptionsContainer = document.getElementById('exceptionsGrid');
    if (!exceptionsContainer) return;
    
    exceptionsContainer.innerHTML = '<div class="loading">加载中...</div>';
    
    // 调用API获取异常情况数据
    fetch('/api/admin/invigilation/exceptions', {
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
            displayExceptions(response.data, exceptionsContainer);
        } else {
            throw new Error(response.message || '加载失败');
        }
    })
    .catch(error => {
        exceptionsContainer.innerHTML = `<div class="error-message">加载失败: ${error.message}<br><button onclick="loadExceptions()">重试</button></div>`;
    });
}

/**
 * 显示异常情况数据
 * @param {Array} exceptions - 异常情况数据
 * @param {HTMLElement} container - 容器元素
 */
function displayExceptions(exceptions, container) {
    if (exceptions.length === 0) {
        container.innerHTML = '<div class="no-data">暂无异常情况记录</div>';
        return;
    }
    
    // 创建异常情况卡片
    container.innerHTML = '';
    exceptions.forEach(exception => {
        const card = document.createElement('div');
        card.className = 'exception-card';
        card.innerHTML = `
            <div class="exception-header">
                <h3>${exception.exceptionType || '未知异常'}</h3>
                <span class="exception-time">${formatDateTime(exception.exceptionTime)}</span>
            </div>
            <div class="exception-body">
                <p><strong>课程:</strong> ${exception.courseName || '未知课程'}</p>
                <p><strong>监考教师:</strong> ${exception.teacherName || '-'}</p>
                <p><strong>异常描述:</strong> ${exception.description || '无描述'}</p>
            </div>
            <div class="exception-footer">
                <button class="btn small-btn details-btn" data-id="${exception.exceptionId}">详情</button>
                <button class="btn small-btn resolve-btn ${exception.status === 'resolved' ? 'disabled' : ''}" data-id="${exception.exceptionId}" ${exception.status === 'resolved' ? 'disabled' : ''}>
                    ${exception.status === 'resolved' ? '已解决' : '标记解决'}
                </button>
            </div>
        `;
        container.appendChild(card);
    });
    
    // 绑定按钮事件
    container.querySelectorAll('.details-btn').forEach(btn => {
        btn.addEventListener('click', function() {
            showExceptionDetails(this.dataset.id);
        });
    });
    
    container.querySelectorAll('.resolve-btn:not(.disabled)').forEach(btn => {
        btn.addEventListener('click', function() {
            resolveException(this.dataset.id);
        });
    });
}

/**
 * 加载统计分析数据
 */
function loadStatistics() {
    const statisticsContent = document.getElementById('statisticsContent');
    if (!statisticsContent) return;
    
    statisticsContent.innerHTML = '<div class="loading">加载中...</div>';
    
    // 获取统计类型
    const typeFilter = document.getElementById('statisticsTypeFilter');
    const statisticsType = typeFilter ? typeFilter.value : 'teacher';
    
    // 调用API获取统计数据
    fetch(`/api/admin/invigilation/statistics?type=${statisticsType}`, {
        headers: getAuthHeaders()
    })
    .then(response => {
        if (!response.ok) {
            throw new Error(response.statusText);
        }
        return response.json();
    })
    .then(response => {
        if (response.code === 200) {
            displayStatistics(response.data, statisticsType, statisticsContent);
        } else {
            throw new Error(response.message || '加载失败');
        }
    })
    .catch(error => {
        statisticsContent.innerHTML = `<div class="error-message">加载失败: ${error.message}<br><button onclick="loadStatistics()">重试</button></div>`;
    });
}

/**
 * 显示统计数据
 * @param {Object} data - 统计数据
 * @param {string} type - 统计类型
 * @param {HTMLElement} container - 容器元素
 */
function displayStatistics(data, type, container) {
    if (!data) {
        container.innerHTML = '<div class="no-data">暂无统计数据</div>';
        return;
    }
    
    // 根据不同的统计类型显示不同的统计图表
    container.innerHTML = '';
    
    // 创建统计标题
    const title = document.createElement('h3');
    title.className = 'section-title';
    
    switch(type) {
        case 'teacher':
            title.textContent = '教师监考统计';
            createTeacherStatistics(data, container);
            break;
        case 'department':
            title.textContent = '部门工作量分析';
            createDepartmentStatistics(data, container);
            break;
        case 'time':
            title.textContent = '监考时长统计';
            createTimeStatistics(data, container);
            break;
        default:
            container.innerHTML = '<div class="no-data">未知统计类型</div>';
            return;
    }
    
    container.insertBefore(title, container.firstChild);
    
    // 绑定生成报表按钮事件
    const reportBtn = document.getElementById('generateReportBtn');
    if (reportBtn) {
        reportBtn.addEventListener('click', function() {
            generateStatisticsReport(type);
        });
    }
}

/**
 * 创建教师监考统计图表
 * @param {Object} data - 统计数据
 * @param {HTMLElement} container - 容器元素
 */
function createTeacherStatistics(data, container) {
    // 检查data.teachers是否存在，如果不存在则显示无数据提示
    if (!data || !data.teachers || !Array.isArray(data.teachers)) {
        container.innerHTML = '<div class="no-data">暂无教师监考统计数据</div>';
        return;
    }
    
    // 创建表格显示教师监考统计
    const table = document.createElement('table');
    table.className = 'data-table';
    table.innerHTML = `
        <thead>
            <tr>
                <th>教师姓名</th>
                <th>所属部门</th>
                <th>监考次数</th>
                <th>总监考时长</th>
                <th>平均评分</th>
            </tr>
        </thead>
        <tbody>
            ${data.teachers.map(teacher => `
                <tr>
                    <td>${teacher.name || '-'}</td>
                    <td>${teacher.department || '-'}</td>
                    <td>${teacher.count || 0}</td>
                    <td>${teacher.totalHours || 0}小时</td>
                    <td>${teacher.averageRating ? teacher.averageRating.toFixed(1) : '-'}</td>
                </tr>
            `).join('')}
        </tbody>
    `;
    
    container.appendChild(table);
}

/**
 * 创建部门工作量分析图表
 * @param {Object} data - 统计数据
 * @param {HTMLElement} container - 容器元素
 */
function createDepartmentStatistics(data, container) {
    // 创建表格显示部门工作量分析
    const table = document.createElement('table');
    table.className = 'data-table';
    table.innerHTML = `
        <thead>
            <tr>
                <th>部门名称</th>
                <th>教师人数</th>
                <th>总监考次数</th>
                <th>总监考时长</th>
                <th>人均监考次数</th>
            </tr>
        </thead>
        <tbody>
            ${data.departments.map(dept => `
                <tr>
                    <td>${dept.name || '-'}</td>
                    <td>${dept.teacherCount || 0}</td>
                    <td>${dept.totalCount || 0}</td>
                    <td>${dept.totalHours || 0}小时</td>
                    <td>${dept.averageCount ? dept.averageCount.toFixed(1) : 0}</td>
                </tr>
            `).join('')}
        </tbody>
    `;
    
    container.appendChild(table);
}

/**
 * 创建监考时长统计图表
 * @param {Object} data - 统计数据
 * @param {HTMLElement} container - 容器元素
 */
function createTimeStatistics(data, container) {
    // 创建表格显示监考时长统计
    const table = document.createElement('table');
    table.className = 'data-table';
    table.innerHTML = `
        <thead>
            <tr>
                <th>月份</th>
                <th>监考次数</th>
                <th>总监考时长</th>
                <th>平均每次时长</th>
            </tr>
        </thead>
        <tbody>
            ${data.months.map(month => `
                <tr>
                    <td>${month.name || '-'}</td>
                    <td>${month.count || 0}</td>
                    <td>${month.totalHours || 0}小时</td>
                    <td>${month.averageHours ? month.averageHours.toFixed(1) : 0}小时</td>
                </tr>
            `).join('')}
        </tbody>
    `;
    
    container.appendChild(table);
}

/**
 * 生成统计报表
 * @param {string} type - 统计类型
 */
function generateStatisticsReport(type) {
    // 调用API生成报表
    fetch(`/api/admin/invigilation/statistics/export?type=${type}`, {
        headers: getAuthHeaders()
    })
    .then(response => response.blob())
    .then(blob => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `监考统计_${type}_${new Date().toLocaleDateString()}.xlsx`;
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
 * 格式化日期时间
 * @param {string} dateTimeStr - 日期时间字符串
 * @returns {string} - 格式化后的日期时间字符串
 */
function formatDateTime(dateTimeStr) {
    if (!dateTimeStr) return '-';
    
    try {
        const date = new Date(dateTimeStr);
        if (isNaN(date.getTime())) return dateTimeStr;
        
        return `${date.getFullYear()}-${(date.getMonth() + 1).toString().padStart(2, '0')}-${date.getDate().toString().padStart(2, '0')} ${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`;
    } catch (e) {
        return dateTimeStr;
    }
}