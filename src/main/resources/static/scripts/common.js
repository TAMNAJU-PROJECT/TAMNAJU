const dialog = document.getElementById('dialog');

if(dialog) {
    dialog.createButton = function (text, onclick) {
        return{
            text: text,
            onclick: onclick
        };
    }

    dialog.hide = function () {
        dialog.classList.remove('visible');
    }

    dialog.show = function (params) {
        const modal = dialog.querySelector(':scope > [rel="modal"]');
        const buttonContainer = modal.querySelector(':scope > [rel="buttonContainer"]');
        modal.querySelector(':scope > [rel="title"]').innerText = params['title'];
        modal.querySelector(':scope > [rel="content"]').innerHTML = params['content'];
        buttonContainer.innerHTML = '';
        if(params['buttons'] && params['buttons'].length > 0) {
            for(const button of params['buttons']) {
                const buttonElement = document.createElement('div');
                buttonElement.classList.add('button');
                buttonElement.innerText = button['text'];
                buttonElement.onclick = button['onclick'];
                buttonContainer.append(buttonElement);
            }
        }
        dialog.classList.add('visible');
    }
}

// if(typeof dialog !== 'undefined' && dialog !== null)  >>   if(dialog)
// if(typeof dialog !== 'undefined' || dialog !== null)  >>   if(!dialog)

const loading = document.getElementById('loading');

if(loading) {
    loading.hide = function () {
        loading.classList.remove('visible');
    }

    loading.show = function () {
        loading.classList.add('visible');
    }
}

//프로토타입이란 아래처럼 사용하면 모든 HTML의 inputElement가 이 testRegex을 가지게 된다.
// input이나 select 등등 상관없이 다 적용할려면 그냥 HTMLElement 으로 사용하면 된다.
HTMLInputElement.prototype.testRegex = function () {
    return new RegExp(this.dataset.regex).test(this.value);
}

HTMLTextAreaElement.prototype.testRegex = HTMLInputElement.prototype.testRegex;