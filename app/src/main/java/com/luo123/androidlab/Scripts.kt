package com.luo123.androidlab

class Scripts() {
    companion object {
        val SCRIPT_FULLSCREEN = """
        $('#header-menu').hide();
        $('#nav-dropdown').removeAttr('');
        $('#content > div > div.visible-xs.visible-sm.pagination-block.text-center.ready > div.wrapper').hide();
        
    """.trimIndent()
         val SCRIPT_SEETING = """
        $('#app-setting').remove(); //因为重复添加所以要先把原本的移除
        $('#main-nav').after(`
        <ul id="app-setting" class="nav navbar-nav pull-left">
					<li class="">
						<a class="navigation-link" href="androidlab://setting" title="" id="" data-original-title="客户端设置">
							
							<i class="fa fa-fw fa-wrench" data-content=""></i>
							<span class="visible-xs-inline">客户端设置</span>
							
						</a>
					</li>
                    <li class="">
                        <a class="navigation-link" href="/topic/21/android-lab-2019-下学期值日表" title="" id="" data-original-title="客户端设置">
                            
                            <i class="fa fa-fw fa-table" data-content=""></i>
                            <span class="visible-xs-inline">值日表</span>
                            
                        </a>
                    </li>
	
        </ul>

`);

        """.trimIndent()

         val DARKMODE = """
function sunMoon() {  
    var styleElem = null,  
    doc = document,  
    ie = doc.all,  
    fontColor = 70,  
    sel = 'body,body *';  
    var styleElem = createCSS(sel, setStyle(fontColor), styleElem);  
 
    if (ie) {  
        doc.attachEvent('onkeydown', onKeyDown);  
    } else {  
        doc.addEventListener('keydown', onKeyDown);  
    };  
    function onKeyDown(evt) {  
        if (! (evt.keyCode === 87 || evt.keyCode === 81)) return;  
        var evt = ie ? window.event: evt;  
        if (evt.keyCode === 87) {  
            fontColor = (fontColor >= 100) ? 100 : fontColor + 10  
        } else if (evt.keyCode === 81) {  
            fontColor = (fontColor <= 10) ? 10 : fontColor - 10  
        };  
        styleElem = createCSS(sel, setStyle(fontColor), styleElem);  
    };  
    function setStyle(fontColor) {  
        var colorArr = [fontColor, fontColor, fontColor];  
        return 'background-color:#000 !important;color:RGB(' + colorArr.join('%,') + '%) !important;'  
    };  
    function createCSS(sel, decl, styleElem) {  
        var doc = document,  
        h = doc.getElementsByTagName('head')[0],  
        styleElem = styleElem;  
        if (!styleElem) {  
            s = doc.createElement('style');  
            s.setAttribute('type', 'text/css');  
            styleElem = ie ? doc.styleSheets[doc.styleSheets.length - 1] : h.appendChild(s);  
        };  
        if (ie) {  
            styleElem.addRule(sel, decl);  
        } else {  
            styleElem.innerHTML = '';  
            styleElem.appendChild(doc.createTextNode(sel + ' {' + decl + '}'));  
        };  
        return styleElem;  
    };  
 
}  
sunMoon();
    """.trimIndent()


    }

}