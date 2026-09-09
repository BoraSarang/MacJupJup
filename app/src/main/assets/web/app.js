/* 맥줍줍 포털 — 바닐라 JS. 타임라인·Watchlist·통계·상세 7섹션 */
(function () {
    'use strict';

    var CATEGORIES = ['생산성', '유틸리티', '보안·프라이버시', '미디어·엔터', '개발',
        '디자인·크리에이티브', '금융', '글쓰기·노트', '시스템최적화', '커뮤니케이션'];
    var LICENSE_LABEL = { OSS: '오픈소스', FREE: '프리', PAID: '유료' };
    var PAGE_SIZE = 20;

    var state = { view: 'timeline', license: '', tag: '', category: '', q: '', sort: 'newest', page: 1, lang: 'ko' };

    /* 한/영 선택: ko 기본, 번역 없으면 원문 폴백 */
    function pick(ko, en) {
        if (state.lang === 'ko' && ko) return { text: ko, isKo: true };
        return { text: en || ko || '', isKo: false };
    }

    function $(id) { return document.getElementById(id); }
    function esc(s) {
        return String(s == null ? '' : s).replace(/[&<>"']/g, function (c) {
            return { '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c];
        });
    }
    function toast(msg) {
        var t = $('toast');
        t.textContent = msg;
        t.hidden = false;
        setTimeout(function () { t.hidden = true; }, 2200);
    }
    function api(path) {
        return fetch(path).then(function (r) {
            if (!r.ok) throw new Error('HTTP ' + r.status);
            return r.json();
        });
    }

    /* ---------- 칩 ---------- */
    function buildCategoryChips() {
        var box = $('categoryChips');
        box.innerHTML = '';
        var all = document.createElement('button');
        all.className = 'chip active';
        all.type = 'button';
        all.textContent = '전체 카테고리';
        all.onclick = function () { state.category = ''; state.page = 1; syncChips(); loadTimeline(); };
        box.appendChild(all);
        CATEGORIES.forEach(function (c) {
            var b = document.createElement('button');
            b.className = 'chip';
            b.type = 'button';
            b.textContent = c;
            b.dataset.category = c;
            b.onclick = function () {
                state.category = state.category === c ? '' : c;
                state.page = 1;
                syncChips();
                loadTimeline();
            };
            box.appendChild(b);
        });
    }
    function syncChips() {
        document.querySelectorAll('#licenseChips .chip').forEach(function (b) {
            var on = (b.dataset.license !== undefined && b.dataset.license === state.license) ||
                (b.dataset.tag !== undefined && b.dataset.tag === state.tag);
            b.classList.toggle('active', !!on);
        });
        document.querySelectorAll('#categoryChips .chip').forEach(function (b) {
            b.classList.toggle('active', (b.dataset.category || '') === state.category);
        });
    }
    function bindLicenseTags() {
        document.querySelectorAll('#licenseChips .chip').forEach(function (b) {
            b.onclick = function () {
                if (b.dataset.license !== undefined) {
                    var v = b.dataset.license;
                    state.license = state.license === v ? '' : v;
                } else {
                    var t = b.dataset.tag;
                    state.tag = state.tag === t ? '' : t;
                }
                state.page = 1;
                syncChips();
                loadTimeline();
            };
        });
    }

    /* ---------- 카드 ---------- */
    function licensePill(a) {
        var cls = a.license === 'OSS' ? 'oss' : a.license === 'PAID' ? 'paid' : 'free';
        return '<span class="pill ' + cls + '">' + esc(LICENSE_LABEL[a.license] || a.license) + '</span>';
    }
    function iconHtml(a) {
        if (a.iconUrl) {
            return '<img class="app-icon" src="' + esc(a.iconUrl) + '" alt="" loading="lazy">';
        }
        var ch = (a.name || '?').trim().charAt(0).toUpperCase();
        return '<span class="app-icon app-icon-ph" aria-hidden="true">' + esc(ch) + '</span>';
    }
    function cardHtml(a) {
        var badges = licensePill(a) +
            '<span class="pill">' + esc(a.category || '') + '</span>' +
            (a.isNew ? '<span class="pill new">NEW</span>' : '') +
            (a.sourceUrl ? '<a class="pill pill-src" target="_blank" rel="noopener" href="' + esc(a.sourceUrl) + '" title="수집 출처에서 보기">출처: ' + esc(a.sourceName || '원문') + '</a>' : '');
        var ver = a.version
            ? '<div class="ver">' + (a.prevVersion ? '<s>' + esc(a.prevVersion) + '</s>' : '') + esc(a.version) + '</div>'
            : '<div class="ver">포착 ' + fmtDate(a.releaseDate || a.firstSeenAt) + '</div>';
        var notes = pick(a.releaseNotesKo, a.releaseNotesSummary);
        if (!notes.text) notes = pick(a.descriptionKo, a.descriptionSnippet);
        var notesHtml = notes.text ? '<div class="notes">' + esc(notes.text) + '</div>' : '';
        var meta = [];
        if (a.stars != null) meta.push('★ ' + a.stars);
        if (a.averageRating != null) meta.push('평점 ' + a.averageRating);
        if (a.primaryLanguage) meta.push(esc(a.primaryLanguage));
        if (a.forks != null) meta.push('⑂ ' + a.forks);
        if (a.fileSize) meta.push(fmtSize(a.fileSize));
        return '<article class="app-card" role="listitem" data-id="' + esc(a.id) + '">' +
            '<div class="app-head">' + iconHtml(a) +
            '<div class="app-head-text"><h3>' + esc(a.name) + '</h3>' +
            '<div class="dev">' + esc(a.developer || '') + '</div></div></div>' +
            '<div class="badges">' + badges + '</div>' + ver + notesHtml +
            '<div class="meta">' + meta.join(' · ') + '</div>' +
            '</article>';
    }
    function fmtDate(ts) {
        if (!ts) return '-';
        var d = new Date(ts);
        return d.getFullYear() + '-' + String(d.getMonth() + 1).padStart(2, '0') + '-' + String(d.getDate()).padStart(2, '0');
    }
    function fmtSize(bytes) {
        if (bytes >= 1073741824) return (bytes / 1073741824).toFixed(1) + 'GB';
        if (bytes >= 1048576) return Math.round(bytes / 1048576) + 'MB';
        return Math.round(bytes / 1024) + 'KB';
    }
    function bindCards(container) {
        container.querySelectorAll('.app-card').forEach(function (el) {
            el.onclick = function () { openDetail(el.dataset.id); };
            // 출처 뱃지 클릭은 새탭만 (모달 방지)
            el.querySelectorAll('a[target="_blank"]').forEach(function (a) {
                a.onclick = function (e) { e.stopPropagation(); };
            });
        });
    }

    /* ---------- 타임라인 ---------- */
    var searchTimer = null;
    function query() {
        var p = new URLSearchParams({ sort: state.sort, page: state.page, pageSize: PAGE_SIZE });
        if (state.license) p.set('license', state.license);
        if (state.tag) p.set('tag', state.tag);
        if (state.category) p.set('category', state.category);
        if (state.q) p.set('q', state.q);
        return '/api/apps?' + p.toString();
    }
    function loadTimeline() {
        var grid = $('appGrid');
        grid.innerHTML = '<div class="empty-state">불러오는 중…</div>';
        api(query()).then(function (d) {
            $('visibleCount').textContent = '검색 결과 ' + d.total + '개';
            if (!d.apps.length) {
                grid.innerHTML = '<div class="empty-state">조건에 맞는 앱이 없습니다</div>';
                $('pagination').innerHTML = '';
                return;
            }
            grid.innerHTML = d.apps.map(cardHtml).join('');
            bindCards(grid);
            renderPagination($('pagination'), d.page, d.pageSize, d.total, function (p) {
                state.page = p;
                loadTimeline();
                window.scrollTo(0, 0);
            });
        }).catch(function () {
            grid.innerHTML = '<div class="empty-state">데이터를 불러오는데 실패했습니다 (서버 미기동 시)</div>';
        });
    }
    function renderPagination(box, page, pageSize, total, go) {
        var pages = Math.max(1, Math.ceil(total / pageSize));
        if (pages <= 1) { box.innerHTML = ''; return; }
        var html = '<button type="button" data-p="' + (page - 1) + '"' + (page <= 1 ? ' disabled' : '') + '>‹</button>';
        var nums = pageNums(page, pages);
        nums.forEach(function (n) {
            if (n === '…') { html += '<button type="button" disabled>…</button>'; return; }
            html += '<button type="button" data-p="' + n + '"' + (n === page ? ' class="active"' : '') + '>' + n + '</button>';
        });
        html += '<button type="button" data-p="' + (page + 1) + '"' + (page >= pages ? ' disabled' : '') + '>›</button>';
        box.innerHTML = html;
        box.querySelectorAll('button[data-p]').forEach(function (b) {
            if (b.disabled) return;
            b.onclick = function () { go(parseInt(b.dataset.p, 10)); };
        });
    }
    function pageNums(page, pages) {
        var set = {};
        [1, 2, page - 1, page, page + 1, pages - 1, pages].forEach(function (n) {
            if (n >= 1 && n <= pages) set[n] = true;
        });
        var arr = Object.keys(set).map(Number).sort(function (a, b) { return a - b; });
        var out = [];
        var prev = 0;
        arr.forEach(function (n) {
            if (n - prev > 1) out.push('…');
            out.push(n);
            prev = n;
        });
        return out;
    }

    /* ---------- Watchlist (버전 업데이트) ---------- */
    function loadWatchlist() {
        var grid = $('watchGrid');
        grid.innerHTML = '<div class="empty-state">불러오는 중…</div>';
        api('/api/apps?sort=updated&page=1&pageSize=' + PAGE_SIZE).then(function (d) {
            var items = d.apps.filter(function (a) { return !a.isNew && a.version; });
            if (!items.length) {
                grid.innerHTML = '<div class="empty-state">아직 버전 업데이트 기록이 없습니다</div>';
                return;
            }
            grid.innerHTML = items.map(cardHtml).join('');
            bindCards(grid);
        }).catch(function () {
            grid.innerHTML = '<div class="empty-state">데이터를 불러오는데 실패했습니다</div>';
        });
    }

    /* ---------- 통계 ---------- */
    function loadStats() {
        var box = $('statsContent');
        box.innerHTML = '<div class="empty-state">통계 불러오는 중…</div>';
        api('/api/stats').then(function (s) {
            return api('/api/stats/trends').then(function (t) { return { s: s, t: t }; });
        }).then(function (r) {
            var s = r.s, t = r.t;
            var cats = Object.keys(t.byCategory || {}).sort(function (a, b) { return t.byCategory[b] - t.byCategory[a]; });
            var maxCat = cats.length ? t.byCategory[cats[0]] : 1;
            var lic = t.byLicense || {};
            var html = '<div class="kpi-grid">' +
                kpi(s.totalApps, '수집 앱') + kpi(s.activeSources, '활성 소스') +
                kpi(t.newLast7d, '최근 7일 신규') + kpi(t.versionBumpsLast7d, '최근 7일 버전업') +
                kpi(t.aiTagCount, 'AI-Agent') + kpi(t.menuBarTagCount, 'MenuBar') +
                '</div><h3>카테고리 분포</h3>';
            cats.forEach(function (c) {
                var v = t.byCategory[c];
                html += '<div class="bar-row"><span class="name">' + esc(c) + '</span>' +
                    '<span class="bar-track"><span class="bar-fill" style="display:block;width:' +
                    Math.round(v / maxCat * 100) + '%"></span></span>' +
                    '<span class="cnt">' + v + '</span></div>';
            });
            html += '<h3>라이선스 분포</h3><div class="bar-row"><span class="name">오픈소스</span>' +
                bar(lic.OSS || 0, s.totalApps) + '</div>' +
                '<div class="bar-row"><span class="name">프리</span>' + bar(lic.FREE || 0, s.totalApps) + '</div>' +
                '<div class="bar-row"><span class="name">유료</span>' + bar(lic.PAID || 0, s.totalApps) + '</div>';
            box.innerHTML = html;
        }).catch(function () {
            box.innerHTML = '<div class="empty-state">통계를 불러오는데 실패했습니다</div>';
        });
    }
    function kpi(n, l) {
        return '<div class="kpi"><div class="n">' + n + '</div><div class="l">' + l + '</div></div>';
    }
    function bar(v, total) {
        var pct = total ? Math.round(v / total * 100) : 0;
        return '<span class="bar-track"><span class="bar-fill" style="display:block;width:' + pct + '%"></span></span>' +
            '<span class="cnt">' + v + '</span>';
    }

    /* ---------- 상세 7섹션 ---------- */
    function openDetail(id) {
        var modal = $('appModal');
        $('appModalTitle').textContent = '불러오는 중…';
        $('appModalBody').innerHTML = '';
        if (typeof modal.showModal === 'function') modal.showModal();
        api('/api/apps/' + encodeURIComponent(id)).then(function (a) {
            $('appModalTitle').textContent = a.name || '앱 상세';
            $('appModalBody').innerHTML = detailHtml(a);
        }).catch(function () {
            $('appModalTitle').textContent = '불러오기 실패';
        });
    }
    function sec(title, inner) {
        if (!inner) return '';
        return '<section class="detail-sec"><h3>' + title + '</h3>' + inner + '</section>';
    }
    function detailHtml(a) {
        // ① 소개 (한국어 기본 + 원문 토글)
        var intro = pick(a.descriptionKo, a.descriptionSnippet);
        var introHtml = intro.text
            ? '<p data-kotext>' + esc(intro.text) + '</p>' + origBtn(a.descriptionKo, a.descriptionSnippet)
            : '';
        // ② 스크린샷 (Apple CDN 직접 표시)
        var shots = '';
        if (a.screenshotUrls) {
            var urls = a.screenshotUrls.split(/\r?\n/).map(function (s) { return s.trim(); }).filter(Boolean);
            if (urls.length) {
                shots = '<div class="shot-row">' + urls.map(function (u) {
                    return '<img src="' + esc(u) + '" alt="스크린샷" loading="lazy">';
                }).join('') + '</div>';
            }
        }
        // ③ 특징
        var feats = [];
        if (a.sellerName) feats.push('<div>판매: ' + esc(a.sellerName) + '</div>');
        if (a.averageRating != null) feats.push('<div>평점 ' + a.averageRating + (a.ratingCount != null ? ' (' + a.ratingCount + '개)' : '') + '</div>');
        if (a.stars != null) feats.push('<div>★ ' + a.stars + (a.forks != null ? ' · ⑂ ' + a.forks : '') + (a.issues != null ? ' · 이슈 ' + a.issues : '') + '</div>');
        if (a.primaryLanguage) feats.push('<div>' + esc(a.primaryLanguage) + '</div>');
        if (a.licenseName) feats.push('<div>라이선스: ' + esc(a.licenseName) + '</div>');
        if (a.fileSize) feats.push('<div>용량: ' + fmtSize(a.fileSize) + '</div>');
        if (a.minOs) feats.push('<div>최소 OS: ' + esc(a.minOs) + '</div>');
        if (a.contentRating) feats.push('<div>연령 등급: ' + esc(a.contentRating) + '</div>');
        if (a.category) feats.push('<div>' + esc(a.category) + '</div>');
        if (a.tags) feats.push('<div>태그: ' + esc(a.tags) + '</div>');
        var feat = feats.length ? '<div class="feat-grid">' + feats.join('') + '</div>' : '';
        // ④ 기능 (본문 — 한국어 기본 + 원문 토글)
        var func = pick(a.descriptionKo, a.description || a.descriptionSnippet);
        var funcHtml = func.text
            ? '<p data-kotext>' + esc(func.text) + '</p>' + origBtn(a.descriptionKo, a.description || a.descriptionSnippet)
            : '';
        // ⑤ 새로운 기능 (한국어 기본 + 원문 토글)
        var newsPick = pick(a.releaseNotesKo, a.releaseNotes || a.releaseNotesSummary);
        var news = '';
        var hasHistory = a.version || (a.versions && a.versions.length) || newsPick.text;
        if (hasHistory) {
            var rows = (a.versions || []).slice(0, 10).map(function (v) {
                return '<tr><td>' + esc(v.version) + '</td><td>' + esc(v.notesSummary || '') + '</td></tr>';
            }).join('');
            news = (newsPick.text
                ? '<p data-kotext>' + esc(newsPick.text) + '</p>' +
                    origBtn(a.releaseNotesKo, a.releaseNotes || a.releaseNotesSummary)
                : '') +
                '<table class="ver-table"><tr><th>버전</th><th>새 기능</th></tr>' +
                (a.version ? '<tr><td><b>' + esc(a.version) + '</b> (현재)' +
                    (a.prevVersion ? ' ← <s>' + esc(a.prevVersion) + '</s>' : '') + '</td><td>' +
                    esc(a.releaseNotesSummary || '') + '</td></tr>' : '') + rows + '</table>';
        } else {
            news = '<p>버전 기록 없음 — ' + fmtDate(a.firstSeenAt) + ' 첫 포착, 다음 업데이트부터 기록됩니다.</p>';
        }
        // ⑥⑦ 홈페이지·다운로드
        var links = [];
        (a.sources || []).forEach(function (s) {
            if (s.sourceUrl) links.push('<a class="btn-link ghost" target="_blank" rel="noopener" href="' + esc(s.sourceUrl) + '">출처: ' + esc(s.sourceName) + '</a>');
        });
        var repo = null;
        (a.sources || []).forEach(function (s) {
            if (!repo && s.sourceUrl && s.sourceUrl.indexOf('github.com') >= 0) repo = s.sourceUrl;
        });
        var dl = '<div class="btn-row">' + links.join('') +
            (repo ? '<a class="btn-link" target="_blank" rel="noopener" href="' + esc(repo) + '">GitHub에서 보기</a>' : '') +
            '</div>';
        return sec('① 소개', introHtml) + sec('② 스크린샷', shots) + sec('③ 특징', feat) +
            sec('④ 기능', funcHtml) + sec('⑤ 새로운 기능', news) +
            sec('⑥ 홈페이지 · ⑦ 다운로드 링크', dl);
    }
    function origBtn(koText, enText) {
        if (!koText || !enText || koText === enText) return '';
        return '<button class="orig-toggle" type="button" data-ko="' + esc(koText) + '" data-en="' + esc(enText) + '">원문보기</button>';
    }
    document.addEventListener('click', function (e) {
        var btn = e.target.closest ? e.target.closest('.orig-toggle') : null;
        if (!btn) return;
        var showingKo = btn.textContent === '원문보기';
        var p = btn.parentElement.querySelector('[data-kotext]');
        if (p) p.textContent = showingKo ? btn.dataset.en : btn.dataset.ko;
        btn.textContent = showingKo ? '한국어보기' : '원문보기';
    });

    /* ---------- 헤더 ---------- */
    function loadHeader() {
        api('/api/stats').then(function (s) {
            $('totalCount').textContent = '총 ' + s.totalApps + '개';
            if (s.lastCollectedAt) {
                $('lastUpdated').textContent = '마지막 업데이트: ' + new Date(s.lastCollectedAt).toLocaleString();
            }
        }).catch(function () { /* 무시 */ });
    }

    /* ---------- 초기화 ---------- */
    function setView(v) {
        state.view = v;
        document.querySelectorAll('.view-tabs .tab-btn').forEach(function (b) {
            var on = b.dataset.view === v;
            b.classList.toggle('active', on);
            b.setAttribute('aria-selected', on ? 'true' : 'false');
        });
        $('timelineSection').hidden = v !== 'timeline';
        $('watchlistSection').hidden = v !== 'watchlist';
        $('statsSection').hidden = v !== 'stats';
        if (v === 'watchlist') loadWatchlist();
        if (v === 'stats') loadStats();
        window.scrollTo(0, 0);
    }
    document.querySelectorAll('.view-tabs .tab-btn').forEach(function (b) {
        b.onclick = function () { setView(b.dataset.view); };
    });
    $('sortSelect').onchange = function (e) { state.sort = e.target.value; state.page = 1; loadTimeline(); };
    $('keyword').addEventListener('input', function (e) {
        clearTimeout(searchTimer);
        searchTimer = setTimeout(function () {
            state.q = e.target.value.trim();
            state.page = 1;
            loadTimeline();
        }, 300);
    });
    $('logoLink').onclick = function () {
        state.license = ''; state.tag = ''; state.category = ''; state.q = ''; state.sort = 'newest'; state.page = 1;
        $('keyword').value = '';
        $('sortSelect').value = 'newest';
        syncChips();
        setView('timeline');
        loadTimeline();
    };
    $('totalCount').onclick = function () {
        if (!confirm('지금 수집할까요?')) return;
        fetch('/api/sync', { method: 'POST', body: '{}' })
            .then(function () { toast('수집 요청됨'); })
            .catch(function () { toast('수집 요청 실패'); });
    };
    $('notifBell').onclick = function () {
        $('appModalTitle').textContent = '알림 센터';
        $('appModalBody').innerHTML = '<div class="empty-state">불러오는 중…</div>';
        if (typeof $('appModal').showModal === 'function') $('appModal').showModal();
        api('/api/notifications?pageSize=20').then(function (d) {
            if (!d.notifications.length) {
                $('appModalBody').innerHTML = '<div class="empty-state">알림이 없습니다</div>';
                return;
            }
            $('appModalBody').innerHTML = d.notifications.map(function (n) {
                return '<section class="detail-sec"><h3>' + esc(n.type) + '</h3><p>' + esc(n.summary) + '</p></section>';
            }).join('');
        }).catch(function () {
            $('appModalBody').innerHTML = '<div class="empty-state">불러오기 실패</div>';
        });
    };
    $('langToggle').onclick = function () {
        state.lang = state.lang === 'ko' ? 'en' : 'ko';
        $('langToggle').textContent = state.lang === 'ko' ? '한국어' : '원문';
        loadTimeline();
        if (state.view === 'watchlist') loadWatchlist();
    };
    $('appModalClose').onclick = function () { $('appModal').close(); };
    $('appModal').addEventListener('click', function (e) {
        if (e.target === $('appModal')) $('appModal').close();
    });

    buildCategoryChips();
    bindLicenseTags();
    syncChips();
    loadHeader();
    loadTimeline();
})();
