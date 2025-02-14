// 检查用户是否已登录
function checkAuth() {
    const token = localStorage.getItem('token');
    console.log('检查认证状态，token:', token ? '存在' : '不存在');

    if (!token) {
        console.log('未找到token，重定向到登录页面');
        window.location.href = '/login';
        return false;
    }

    // 设置全局的AJAX请求头
    $.ajaxSetup({
        beforeSend: function (xhr) {
            xhr.setRequestHeader('Authorization', 'Bearer ' + token);
        }
    });

    // 发送验证请求
    $.ajax({
        url: '/api/auth/verify',
        type: 'GET',
        success: function (response) {
            console.log('Token验证响应:', response);
            if (!response.data) {
                console.log('Token验证失败');
                localStorage.removeItem('token');
                window.location.href = '/login';
                return false;
            }
            return true;
        },
        error: function (xhr) {
            console.log('Token验证请求失败:', xhr.status);
            localStorage.removeItem('token');
            window.location.href = '/login';
            return false;
        }
    });

    return true;
}

// 处理页面导航
function handleNavigation() {
    $('.sidebar-item').click(function (e) {
        e.preventDefault();
        const href = $(this).data('href');
        console.log('点击导航:', href);

        if (href === '/logout') {
            console.log('执行登出操作');
            localStorage.removeItem('token');
            window.location.href = '/login';
            return;
        }

        const token = localStorage.getItem('token');
        if (!token) {
            console.log('未找到token，重定向到登录页面');
            window.location.href = '/login';
            return;
        }

        // 创建一个表单来处理导航
        const form = document.createElement('form');
        form.method = 'POST';
        form.action = href;

        // 添加token作为隐藏字段
        const tokenInput = document.createElement('input');
        tokenInput.type = 'hidden';
        tokenInput.name = 'token';
        tokenInput.value = token;
        form.appendChild(tokenInput);

        // 添加Authorization header作为隐藏字段
        const authInput = document.createElement('input');
        authInput.type = 'hidden';
        authInput.name = 'Authorization';
        authInput.value = 'Bearer ' + token;
        form.appendChild(authInput);

        // 添加到body并提交
        document.body.appendChild(form);
        console.log('提交导航表单到:', href);
        form.submit();
    });
}

// 在页面加载时执行
$(document).ready(function () {
    console.log('页面加载完成，开始检查认证状态');
    if (!checkAuth()) {
        return;
    }
    console.log('认证检查通过，设置导航处理');
    handleNavigation();
}); 