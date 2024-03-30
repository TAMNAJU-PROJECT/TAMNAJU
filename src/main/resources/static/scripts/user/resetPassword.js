const resetPasswordForm = document.getElementById('resetPasswordForm');

resetPasswordForm['emailSend'].onclick = function () {

    if (resetPasswordForm['email'].value === '') {
        dialog.show({
            title: '이메일',
            content: '이메일을 입력해 주세요.',
            buttons: [
                dialog.createButton('확인', function () {
                    resetPasswordForm['email'].focus();
                    dialog.hide();
                })
            ]
        });
        return false;
    }
    if (!new RegExp(resetPasswordForm['email'].dataset.regex).test(resetPasswordForm['email'].value)) {
        dialog.show({
            title: '이메일',
            content: '올바른 이메일을 입력해 주세요.',
            buttons: [
                dialog.createButton('확인', function () {
                    resetPasswordForm['email'].focus();
                    resetPasswordForm['email'].select();
                    dialog.hide();
                })
            ]
        });
        return false;
    }
    const xhr = new XMLHttpRequest();  //이메일 인증번호 전송을 요청하기 위한 XHR 객체
    const formData = new FormData(); // 요청을 보낼 때 이메일을 같이 보내기 위한 FormData 객체
    formData.append('email', resetPasswordForm['email'].value);
    xhr.onreadystatechange = function () {
        if (xhr.readyState !== XMLHttpRequest.DONE) {
            return false;
        }
        loading.hide();
        if (xhr.status < 200 || xhr.status >= 300) {
            dialog.show({
                title: '오류',
                content: '요청을 전송하는 도중 예상치 못한 오류가 발생하였습니다.<br><br>잠시 후 다시 시도해 주세요.',
                buttons: [dialog.createButton('확인', dialog.hide)]
            });
            return;
        }
        const responseObject = JSON.parse(xhr.responseText);
        switch (responseObject['result']) {
            case 'failure':
                dialog.show({
                    title: '오류',
                    content: '알 수 없는 이유로 인증번호를 전송하지 못하였습니다.<br><br>잠시 후 다시 시도해 주세요.',
                    buttons: [dialog.createButton('확인', dialog.hide)]
                });
                break;
            case 'failure_unknown_email':
                dialog.show({
                    title: '오류',
                    content: '해당 정보로 회원을 찾을 수 없습니다.',
                    buttons: [
                        dialog.createButton('확인', function () {
                            resetPasswordForm['email'].focus();
                            resetPasswordForm['email'].select();
                            dialog.hide();
                        })
                    ]
                });
                break;
            case 'success':
                resetPasswordForm['emailSalt'].value = responseObject['salt'];
                resetPasswordForm['email'].setAttribute('disabled', '');
                resetPasswordForm['emailSend'].setAttribute('disabled', '');
                resetPasswordForm['emailCode'].removeAttribute('disabled');
                resetPasswordForm['emailVerify'].removeAttribute('disabled');
                dialog.show({
                    title: '성공',
                    content: '입력하신 이메일로 인증번호가 포함된 메일을 전송하였습니다.<br><br>해당 인증번호는 <b>5분간만 유효</b>하니 유의해 주세요.',
                    buttons: [
                        dialog.createButton('확인', function () {
                            dialog.hide();
                            resetPasswordForm['emailCode'].focus();
                        })
                    ]
                });
                break;
            default:
                dialog.show({
                    title: '오류',
                    content: '서버가 예상치 못한 응답을 반환하였습니다.<br><br>잠시 후 다시 시도해 주세요.',
                    buttons: [dialog.createButton('확인', dialog.hide)]
                });
        }
    }
    xhr.open('POST', './resetPasswordEmail'); //get 방식으로 보낼 때는 뒤에 ?
    xhr.send(formData);
    loading.show();
}

resetPasswordForm['emailVerify'].onclick = function () {
    if (resetPasswordForm['emailCode'].value === '') {
        dialog.show({
            title: '이메일 인증번호',
            content: '이메일 인증번호를 입력해 주세요.',
            buttons: [
                dialog.createButton('확인', function () {
                    dialog.hide();
                    resetPasswordForm['emailCode'].focus();
                })
            ]
        });
        return false;
    }
    if (!new RegExp(resetPasswordForm['emailCode'].dataset.regex).test(resetPasswordForm['emailCode'].value)) {
        dialog.show({
            title: '이메일 인증번호',
            content: '올바른 이메일 인증번호를 입력해 주세요.',
            buttons: [
                dialog.createButton('확인', function () {
                    dialog.hide();
                    resetPasswordForm['emailCode'].focus();
                    resetPasswordForm['emailCode'].select();
                })
            ]
        });
        return false;
    }

    const xhr = new XMLHttpRequest();
    const formData = new FormData();
    formData.append('email', resetPasswordForm['email'].value);
    formData.append('code', resetPasswordForm['emailCode'].value);
    formData.append('salt', resetPasswordForm['emailSalt'].value);
    xhr.onreadystatechange = function () {
        if (xhr.readyState !== XMLHttpRequest.DONE) {
            return false;
        }
        if (xhr.status < 200 || xhr.status >= 300) {
            dialog.show({
                title: '오류',
                content: '요청을 전송하는 도중 예상치 못한 오류가 발생하였습니다.<br><br>잠시 후 다시 시도해 주세요.',
                buttons: [dialog.createButton('확인', dialog.hide)]
            });
            return false;
        }
        const responseObject = JSON.parse(xhr.responseText);
        switch (responseObject['result']) {
            case 'failure':
                dialog.show({
                    title: '오류',
                    content: '알 수 없는 이유로 이메일 인증번호를 확인하지 못하였습니다.<br><br>잠시 후 다시 시도해 주세요.',
                    buttons: [dialog.createButton('확인', dialog.hide)]
                });
                break;
            case 'failure_expired':
                dialog.show({
                    title: '오류',
                    content: '이메일 인증번호 세션이 만료되었습니다.<br><br>아래 확인 버튼을 눌러 이메일 인증을 재진행해 주세요.',
                    buttons: [
                        dialog.createButton('확인', function () {
                            dialog.hide();
                            resetPasswordForm['emailSalt'].value = '';
                            resetPasswordForm['email'].removeAttribute('disabled');
                            // disabled 라는 Attribute를 없애겠다.
                            resetPasswordForm['email'].focus();
                            resetPasswordForm['email'].select();
                            resetPasswordForm['emailSend'].removeAttribute('disabled');
                            resetPasswordForm['emailCode'].value = '';
                            resetPasswordForm['emailCode'].setAttribute('disabled', '');
                            resetPasswordForm['emailVerify'].setAttribute('disabled', '');
                        })
                    ]
                });
                break;
            case 'failure_invalid_code':
                dialog.show({
                    title: '오류',
                    content: '이메일 인증번호가 올바르지 않습니다.<br><br>입력하신 인증번호를 다시 확인해 주세요.',
                    buttons: [
                        dialog.createButton('확인', function () {
                            dialog.hide();
                            resetPasswordForm['emailCode'].focus();
                            resetPasswordForm['emailCode'].select();
                        })
                    ]
                });
                break;
            case 'success':
                dialog.show({
                    title: '이메일 인증',
                    content: '이메일 및 인증번호를 확인하였습니다.<br><br>신규 비밀번호를 입력하고 비밀번호 재설정 버튼을 클릭해 주세요.',
                    buttons: [
                        dialog.createButton('확인', function () {
                            dialog.hide();
                            resetPasswordForm['emailCode'].setAttribute('disabled', '');
                            resetPasswordForm['emailVerify'].setAttribute('disabled', '');
                            resetPasswordForm['password'].removeAttribute('disabled');
                            resetPasswordForm['password'].focus();
                            resetPasswordForm['passwordCheck'].removeAttribute('disabled');
                            resetPasswordForm['submit'].removeAttribute('disabled');
                        })
                    ]
                });
                break;
            default:
                dialog.show({
                    title: '오류',
                    content: '서버가 예상치 못한 응답을 반환하였습니다.<br><br>잠시 후 다시 시도해 주세요.',
                    buttons: [dialog.createButton('확인', dialog.hide)]
                });
        }
    };
    xhr.open('PATCH', './resetPasswordEmail');
    xhr.send(formData);
}

resetPasswordForm.onsubmit = function (e) {
    e.preventDefault();

    if (resetPasswordForm['password'].value === '') {
        dialog.show({
            title: '경고',
            content: '신규 비밀번호를 입력해 주세요.',
            buttons: [dialog.createButton('확인', function () {
                dialog.hide();
                resetPasswordForm['password'].focus();
            })]
        });
        return false;
    }
    if (!resetPasswordForm['password'].testRegex()) {
        dialog.show({
            title: '경고',
            content: '올바른 신규 비밀번호를 입력해 주세요.',
            buttons: [dialog.createButton('확인', function () {
                dialog.hide();
                resetPasswordForm['password'].focus();
                resetPasswordForm['password'].select();
            })]
        });
        return false;
    }
    if (resetPasswordForm['passwordCheck'].value === '') {
        dialog.show({
            title: '경고',
            content: '신규 비밀번호를 한번 더 입력해 주세요.',
            buttons: [dialog.createButton('확인', function () {
                dialog.hide();
                resetPasswordForm['passwordCheck'].focus();
            })]
        });
        return false;
    }
    if (resetPasswordForm['password'].value !== resetPasswordForm['passwordCheck'].value) {
        dialog.show({
            title: '경고',
            content: '신규 비밀번호가 일치하지 않습니다.',
            buttons: [dialog.createButton('확인', function () {
                dialog.hide();
                resetPasswordForm['passwordCheck'].focus();
                resetPasswordForm['passwordCheck'].select();
            })]
        });
        return false;
    }

    const xhr = new XMLHttpRequest();
    const formData = new FormData();
    formData.append('email', resetPasswordForm['email'].value);
    formData.append('code', resetPasswordForm['emailCode'].value);
    formData.append('salt', resetPasswordForm['emailSalt'].value);
    formData.append('password', resetPasswordForm['password'].value);
    xhr.onreadystatechange = function () {
        if (xhr.readyState !== XMLHttpRequest.DONE) {
            return false;
        }
        loading.hide();
        if (xhr.status < 200 || xhr.status >= 300) {
            dialog.show({
                title: '오류',
                content: '요청을 전송하는 도중 예상치 못한 오류가 발생하였습니다.<br><br>잠시 후 다시 시도해 주세요.',
                buttons: [dialog.createButton('확인', dialog.hide)]
            });
            return false;
        }
        const responseObject = JSON.parse(xhr.responseText);
        switch (responseObject['result']) {
            case 'failure':
                dialog.show({
                    title: '오류',
                    content: '알 수 없는 이유로 비밀번호를 변경하지 못하였습니다.<br><br>잠시 후 다시 시도해 주세요.',
                    buttons: [dialog.createButton('확인', dialog.hide)]
                });
                break;
            case 'success':
                dialog.show({
                    title: '비밀번호 재설정',
                    content: '비밀번호를 성공적으로 변경하였습니다.<br><br>아래 <b>로그인하러 가기</b>버튼을 클릭하여 로그인해 주세요.',
                    buttons: [dialog.createButton('로그인하러 가기', function () {
                        location.href = './login';
                    })]
                });
                break;
            default:
                dialog.show({
                    title: '오류',
                    content: '서버가 예상치 못한 응답을 반환하였습니다.<br><br>잠시 후 다시 시도해 주세요.',
                    buttons: [dialog.createButton('확인', dialog.hide)]
                });
        }
    };// get은 정보를 받을 때, post는 정보를 가져올 때, patch는 정보를 수정할 때 주로 사용한다.
    xhr.open('PATCH', './resetPassword'); // xhr.open에서 PATCH와 ./registerEmail를 통해 무슨 값을 받아올지 판단한다.
    xhr.send(formData); // 서버로 formData 에 있는 정보를 내보내겠다.
    loading.show();
}
