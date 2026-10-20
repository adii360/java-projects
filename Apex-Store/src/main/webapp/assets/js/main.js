document.addEventListener('DOMContentLoaded', () => {
    // Quantity increment & decrement controls
    document.querySelectorAll('.qty-control').forEach(control => {
        const input = control.querySelector('.qty-input');
        const btnMinus = control.querySelector('.btn-minus') || control.querySelector('#bn_minus_btn') || control.querySelector('#modalQtyMinus');
        const btnPlus = control.querySelector('.btn-plus') || control.querySelector('#bn_plus_btn') || control.querySelector('#modalQtyPlus');

        if (btnMinus && input) {
            btnMinus.addEventListener('click', () => {
                let current = parseInt(input.value) || 1;
                if (current > 1) {
                    input.value = current - 1;
                    input.dispatchEvent(new Event('change'));
                }
            });
        }

        if (btnPlus && input) {
            btnPlus.addEventListener('click', () => {
                let current = parseInt(input.value) || 1;
                const max = parseInt(input.getAttribute('max')) || 999;
                if (current < max) {
                    input.value = current + 1;
                    input.dispatchEvent(new Event('change'));
                }
            });
        }
    });

    // Confirmation dialogs for dangerous actions
    document.querySelectorAll('[data-confirm]').forEach(el => {
        el.addEventListener('click', (e) => {
            const message = el.getAttribute('data-confirm') || 'Are you sure?';
            if (!confirm(message)) {
                e.preventDefault();
            }
        });
    });

    // Auto-dismiss alerts after 5 seconds
    document.querySelectorAll('.alert').forEach(alert => {
        setTimeout(() => {
            alert.style.transition = 'opacity 0.5s ease';
            alert.style.opacity = '0';
            setTimeout(() => alert.remove(), 500);
        }, 5000);
    });

    // Make entire product-card clickable to view product details
    document.querySelectorAll('.product-card[data-href]').forEach(card => {
        card.addEventListener('click', (e) => {
            // Do not navigate if clicking on buttons, forms, wishlist toggle, or inner links
            if (e.target.closest('button, a, form, input, select, textarea, .wishlist-toggle, .btn-buy-now')) {
                return;
            }
            const url = card.getAttribute('data-href');
            if (url) {
                window.location.href = url;
            }
        });
    });

    // =========================================================================
    // Buy Now Modal Logic
    // =========================================================================
    const modalOverlay = document.getElementById('buyNowModalOverlay');
    const closeBtn = document.getElementById('closeBuyNowModal');
    const cancelBtn = document.getElementById('cancelBuyNowBtn');
    const backdrop = document.getElementById('buyNowModalBackdrop');
    const modalForm = document.getElementById('modalBuyNowForm');

    const modalProductId = document.getElementById('modalProductId');
    const modalProductImg = document.getElementById('modalProductImg');
    const modalProductName = document.getElementById('modalProductName');
    const modalProductPrice = document.getElementById('modalProductPrice');
    const modalProductQty = document.getElementById('modalProductQty');
    const modalQtyMinus = document.getElementById('modalQtyMinus');
    const modalQtyPlus = document.getElementById('modalQtyPlus');

    const modalSubtotalVal = document.getElementById('modalSubtotalVal');
    const modalShippingVal = document.getElementById('modalShippingVal');
    const modalGrandTotalVal = document.getElementById('modalGrandTotalVal');
    const modalSubmitBtn = document.getElementById('modalSubmitBtn');

    const modalPayCod = document.getElementById('modalPayCod');
    const modalPayOnline = document.getElementById('modalPayOnline');
    const modalPayCodCard = document.getElementById('modalPayCodCard');
    const modalPayOnlineCard = document.getElementById('modalPayOnlineCard');
    const modalOnlineDetails = document.getElementById('modalOnlineDetails');

    let currentUnitPrice = 0;

    function formatCurrency(num) {
        return '₹' + Number(num).toLocaleString('en-IN', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
    }

    function calculateModalTotals() {
        if (!modalProductQty) return;
        const qty = Math.max(1, parseInt(modalProductQty.value) || 1);
        const subtotal = currentUnitPrice * qty;
        const shipping = subtotal >= 999.00 ? 0 : 99.00;
        const total = subtotal + shipping;

        if (modalSubtotalVal) modalSubtotalVal.textContent = formatCurrency(subtotal);
        if (modalShippingVal) {
            modalShippingVal.textContent = shipping === 0 ? 'FREE' : formatCurrency(shipping);
            modalShippingVal.style.color = shipping === 0 ? 'var(--success)' : 'inherit';
        }
        if (modalGrandTotalVal) modalGrandTotalVal.textContent = formatCurrency(total);

        if (modalSubmitBtn) {
            const isOnline = modalPayOnline && modalPayOnline.checked;
            if (isOnline) {
                modalSubmitBtn.innerHTML = '<i class="fa-solid fa-lock"></i> Pay ' + formatCurrency(total) + ' Online';
                modalSubmitBtn.style.background = 'linear-gradient(135deg, #4f46e5, #7c3aed)';
            } else {
                modalSubmitBtn.innerHTML = '<i class="fa-solid fa-bolt"></i> Place Order with COD (' + formatCurrency(total) + ')';
                modalSubmitBtn.style.background = 'linear-gradient(135deg, #f59e0b, #ea580c)';
            }
        }
    }

    function openBuyNowModal(productData) {
        if (!modalOverlay) return;

        currentUnitPrice = parseFloat(productData.price) || 0;
        if (modalProductId) modalProductId.value = productData.id || '';
        if (modalProductImg) {
            modalProductImg.src = productData.image || '';
            modalProductImg.alt = productData.name || 'Product';
        }
        if (modalProductName) modalProductName.textContent = productData.name || '';
        if (modalProductPrice) modalProductPrice.textContent = formatCurrency(currentUnitPrice);
        if (modalProductQty) {
            modalProductQty.value = productData.quantity || 1;
            modalProductQty.max = productData.stock || 999;
        }

        // Reset to Cash on Delivery default
        if (modalPayCod) {
            modalPayCod.checked = true;
            if (modalPayCodCard) modalPayCodCard.classList.add('active');
            if (modalPayOnlineCard) modalPayOnlineCard.classList.remove('active');
            if (modalOnlineDetails) modalOnlineDetails.style.display = 'none';
        }

        calculateModalTotals();

        modalOverlay.style.display = 'flex';
        modalOverlay.setAttribute('aria-hidden', 'false');
        document.body.style.overflow = 'hidden';
    }

    function closeBuyNowModal() {
        if (!modalOverlay) return;
        modalOverlay.style.display = 'none';
        modalOverlay.setAttribute('aria-hidden', 'true');
        document.body.style.overflow = '';
    }

    if (closeBtn) closeBtn.addEventListener('click', closeBuyNowModal);
    if (cancelBtn) cancelBtn.addEventListener('click', closeBuyNowModal);
    if (backdrop) backdrop.addEventListener('click', closeBuyNowModal);

    document.addEventListener('keydown', (e) => {
        if (e.key === 'Escape' && modalOverlay && modalOverlay.style.display !== 'none') {
            closeBuyNowModal();
        }
    });

    if (modalQtyMinus && modalProductQty) {
        modalQtyMinus.addEventListener('click', () => {
            let val = parseInt(modalProductQty.value) || 1;
            if (val > 1) {
                modalProductQty.value = val - 1;
                calculateModalTotals();
            }
        });
    }

    if (modalQtyPlus && modalProductQty) {
        modalQtyPlus.addEventListener('click', () => {
            let val = parseInt(modalProductQty.value) || 1;
            const max = parseInt(modalProductQty.getAttribute('max')) || 999;
            if (val < max) {
                modalProductQty.value = val + 1;
                calculateModalTotals();
            }
        });
    }

    if (modalProductQty) {
        modalProductQty.addEventListener('input', calculateModalTotals);
        modalProductQty.addEventListener('change', calculateModalTotals);
    }

    // Modal Payment Method Toggle
    if (modalPayCod) {
        modalPayCod.addEventListener('change', () => {
            if (modalPayCod.checked) {
                if (modalPayCodCard) modalPayCodCard.classList.add('active');
                if (modalPayOnlineCard) modalPayOnlineCard.classList.remove('active');
                if (modalOnlineDetails) modalOnlineDetails.style.display = 'none';
                calculateModalTotals();
            }
        });
    }

    if (modalPayOnline) {
        modalPayOnline.addEventListener('change', () => {
            if (modalPayOnline.checked) {
                if (modalPayOnlineCard) modalPayOnlineCard.classList.add('active');
                if (modalPayCodCard) modalPayCodCard.classList.remove('active');
                if (modalOnlineDetails) modalOnlineDetails.style.display = 'block';
                calculateModalTotals();
            }
        });
    }

    // Modal Online Sub-Tabs (UPI / Card / NetBanking)
    const modalTabUpi = document.getElementById('modalTabUpi');
    const modalTabCard = document.getElementById('modalTabCard');
    const modalTabNetBanking = document.getElementById('modalTabNetBanking');
    const modalSubboxUpi = document.getElementById('modalSubboxUpi');
    const modalSubboxCard = document.getElementById('modalSubboxCard');
    const modalSubboxNetBanking = document.getElementById('modalSubboxNetBanking');

    function switchModalOnlineTab(tab) {
        [modalTabUpi, modalTabCard, modalTabNetBanking].forEach(t => t && t.classList.remove('active'));
        [modalSubboxUpi, modalSubboxCard, modalSubboxNetBanking].forEach(b => b && (b.style.display = 'none'));

        if (tab === 'UPI') {
            if (modalTabUpi) modalTabUpi.classList.add('active');
            if (modalSubboxUpi) modalSubboxUpi.style.display = 'block';
            const r = modalTabUpi?.querySelector('input');
            if (r) r.checked = true;
        } else if (tab === 'CARD') {
            if (modalTabCard) modalTabCard.classList.add('active');
            if (modalSubboxCard) modalSubboxCard.style.display = 'block';
            const r = modalTabCard?.querySelector('input');
            if (r) r.checked = true;
        } else if (tab === 'NETBANKING') {
            if (modalTabNetBanking) modalTabNetBanking.classList.add('active');
            if (modalSubboxNetBanking) modalSubboxNetBanking.style.display = 'block';
            const r = modalTabNetBanking?.querySelector('input');
            if (r) r.checked = true;
        }
    }

    if (modalTabUpi) modalTabUpi.addEventListener('click', () => switchModalOnlineTab('UPI'));
    if (modalTabCard) modalTabCard.addEventListener('click', () => switchModalOnlineTab('CARD'));
    if (modalTabNetBanking) modalTabNetBanking.addEventListener('click', () => switchModalOnlineTab('NETBANKING'));

    // Attach click handlers to all .btn-buy-now across pages
    document.querySelectorAll('.btn-buy-now').forEach(btn => {
        btn.addEventListener('click', (e) => {
            e.preventDefault();
            e.stopPropagation();

            let qty = 1;
            // If on product detail page, read selected quantity
            const detailQtyInput = document.querySelector('form .qty-control input[name="quantity"]');
            if (btn.classList.contains('detail-buy-now-btn') && detailQtyInput) {
                qty = parseInt(detailQtyInput.value) || 1;
            }

            const productData = {
                id: btn.getAttribute('data-id'),
                name: btn.getAttribute('data-name'),
                price: btn.getAttribute('data-price'),
                image: btn.getAttribute('data-image'),
                stock: btn.getAttribute('data-stock'),
                quantity: qty
            };

            openBuyNowModal(productData);
        });
    });

    // =========================================================================
    // Standalone Checkout & BuyNow Page Payment Toggles
    // =========================================================================
    function setupPagePaymentToggle(codId, onlineId, codWrapId, onlineWrapId, detailsId, submitBtnId) {
        const codRadio = document.getElementById(codId);
        const onlineRadio = document.getElementById(onlineId);
        const codWrap = document.getElementById(codWrapId);
        const onlineWrap = document.getElementById(onlineWrapId);
        const onlineDetails = document.getElementById(detailsId);
        const submitBtn = document.getElementById(submitBtnId);

        if (!codRadio || !onlineRadio) return;

        function refresh() {
            if (codRadio.checked) {
                if (codWrap) {
                    codWrap.style.borderColor = 'var(--success)';
                    codWrap.style.background = 'var(--success-light)';
                }
                if (onlineWrap) {
                    onlineWrap.style.borderColor = 'var(--border)';
                    onlineWrap.style.background = 'white';
                }
                if (onlineDetails) onlineDetails.style.display = 'none';
                if (submitBtn) {
                    submitBtn.innerHTML = '<i class="fa-solid fa-check"></i> Place Order with Cash on Delivery';
                    submitBtn.style.background = 'linear-gradient(135deg, #f59e0b, #ea580c)';
                }
            } else if (onlineRadio.checked) {
                if (onlineWrap) {
                    onlineWrap.style.borderColor = 'var(--primary)';
                    onlineWrap.style.background = 'rgba(99, 102, 241, 0.05)';
                }
                if (codWrap) {
                    codWrap.style.borderColor = 'var(--border)';
                    codWrap.style.background = 'white';
                }
                if (onlineDetails) onlineDetails.style.display = 'block';
                if (submitBtn) {
                    submitBtn.innerHTML = '<i class="fa-solid fa-lock"></i> Pay Online & Confirm Order';
                    submitBtn.style.background = 'linear-gradient(135deg, #4f46e5, #7c3aed)';
                }
            }
        }

        codRadio.addEventListener('change', refresh);
        onlineRadio.addEventListener('change', refresh);
        if (codWrap) codWrap.addEventListener('click', (e) => {
            if (e.target.tagName !== 'INPUT') {
                codRadio.checked = true;
                refresh();
            }
        });
        if (onlineWrap) onlineWrap.addEventListener('click', (e) => {
            if (e.target.tagName !== 'INPUT') {
                onlineRadio.checked = true;
                refresh();
            }
        });

        // Online Sub-chips (UPI / Card / NetBanking)
        const chips = onlineWrap ? onlineWrap.querySelectorAll('.online-type-chip') : [];
        chips.forEach(chip => {
            chip.addEventListener('click', () => {
                chips.forEach(c => c.classList.remove('active'));
                chip.classList.add('active');

                const val = chip.querySelector('input')?.value;
                const prefix = codId.startsWith('bn_') ? 'bn_' : '';
                const upiBox = document.getElementById(prefix + 'upi_box');
                const cardBox = document.getElementById(prefix + 'card_box');
                const netBox = document.getElementById(prefix + 'netbanking_box');

                if (upiBox) upiBox.style.display = val === 'UPI' ? 'block' : 'none';
                if (cardBox) cardBox.style.display = val === 'CARD' ? 'block' : 'none';
                if (netBox) netBox.style.display = val === 'NETBANKING' ? 'block' : 'none';
            });
        });
    }

    // Checkout page
    setupPagePaymentToggle('pay_cod', 'pay_online', 'pay_cod_wrapper', 'pay_online_wrapper', 'online_payment_details', 'checkout_submit_btn');

    // Standalone buynow.jsp page
    setupPagePaymentToggle('bn_pay_cod', 'bn_pay_online', 'bn_pay_cod_wrapper', 'bn_pay_online_wrapper', 'bn_online_details', 'bn_submit_btn');

    // buynow.jsp live quantity updater
    const bnQtyInput = document.getElementById('bn_qty_input');
    const bnMinusBtn = document.getElementById('bn_minus_btn');
    const bnPlusBtn = document.getElementById('bn_plus_btn');
    const bnSubtotalDisp = document.getElementById('bn_subtotal_display');
    const bnShippingDisp = document.getElementById('bn_shipping_display');
    const bnTotalDisp = document.getElementById('bn_total_display');
    const unitPriceEl = document.getElementById('unitPriceDisplay');

    if (bnQtyInput && unitPriceEl) {
        const uPrice = parseFloat(unitPriceEl.getAttribute('data-price')) || 0;

        function updateBuyNowPageTotals() {
            const q = Math.max(1, parseInt(bnQtyInput.value) || 1);
            const sub = uPrice * q;
            const ship = sub >= 999.00 ? 0 : 99.00;
            const tot = sub + ship;

            if (bnSubtotalDisp) bnSubtotalDisp.textContent = formatCurrency(sub);
            if (bnShippingDisp) {
                bnShippingDisp.innerHTML = ship === 0 ? '<strong style="color: var(--success);">FREE</strong>' : formatCurrency(ship);
            }
            if (bnTotalDisp) bnTotalDisp.textContent = formatCurrency(tot);
        }

        if (bnMinusBtn) {
            bnMinusBtn.addEventListener('click', () => {
                let v = parseInt(bnQtyInput.value) || 1;
                if (v > 1) {
                    bnQtyInput.value = v - 1;
                    updateBuyNowPageTotals();
                }
            });
        }
        if (bnPlusBtn) {
            bnPlusBtn.addEventListener('click', () => {
                let v = parseInt(bnQtyInput.value) || 1;
                const m = parseInt(bnQtyInput.getAttribute('max')) || 999;
                if (v < m) {
                    bnQtyInput.value = v + 1;
                    updateBuyNowPageTotals();
                }
            });
        }
        bnQtyInput.addEventListener('input', updateBuyNowPageTotals);
        bnQtyInput.addEventListener('change', updateBuyNowPageTotals);
    }
});
