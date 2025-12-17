import { useState, useEffect } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { Plus, List, Edit2, Trash2, Power, PowerOff, Save, X, Search } from 'lucide-react';
import ConfirmModal from './ConfirmModal';
import Portal from './Portal';

/**
 * Category from backend with specific filter fields.
 */
export interface Category {
    id: number;
    name: string;
    description: string;
    fromFilter: string;
    subjectKeywords: string;
    bodyKeywords: string;
    isActive: boolean;
    generatedQuery: string;
    createdAt: string;
    updatedAt: string;
}

/**
 * Component to manage email monitoring categories.
 */
const CategoryManager: React.FC = () => {
    const [categories, setCategories] = useState<Category[]>([]);
    const [loading, setLoading] = useState(false);
    const [editingId, setEditingId] = useState<number | null>(null);
    const [showAddForm, setShowAddForm] = useState(false);
    const [showListModal, setShowListModal] = useState(false);
    const [formData, setFormData] = useState({
        name: '',
        description: '',
        fromFilter: '',
        subjectKeywords: '',
        bodyKeywords: '',
        isActive: true,
    });

    // Modal states
    const [confirmModal, setConfirmModal] = useState({
        isOpen: false,
        title: '',
        message: '',
        type: 'confirm' as 'confirm' | 'success' | 'error' | 'info',
        onConfirm: () => { },
    });

    const API_BASE = 'http://localhost:8086/api/v1';

    // Load categories on mount
    useEffect(() => {
        loadCategories();
    }, []);

    const loadCategories = async () => {
        setLoading(true);
        try {
            const response = await fetch(`${API_BASE}/categories`);
            const data = await response.json();
            setCategories(data);
        } catch (error) {
            console.error('Error loading categories:', error);
        } finally {
            setLoading(false);
        }
    };

    const showMessage = (title: string, message: string, type: 'success' | 'error' | 'info') => {
        setConfirmModal({
            isOpen: true,
            title,
            message,
            type,
            onConfirm: () => { },
        });
    };

    const handleAdd = async () => {
        if (!formData.name.trim()) {
            showMessage('Campo Obrigatório', 'Preencha o nome da categoria.', 'error');
            return;
        }

        if (!formData.fromFilter.trim() && !formData.subjectKeywords.trim() && !formData.bodyKeywords.trim()) {
            showMessage('Filtro Obrigatório', 'Preencha pelo menos um filtro (remetente, assunto ou palavras-chave).', 'error');
            return;
        }

        try {
            const response = await fetch(`${API_BASE}/categories`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(formData),
            });

            if (response.ok) {
                await loadCategories();
                setShowAddForm(false);
                resetForm();
                showMessage('Sucesso!', 'Categoria adicionada com sucesso.', 'success');
            } else {
                const errorText = await response.text();
                showMessage('Erro', `Falha ao adicionar categoria: ${errorText}`, 'error');
            }
        } catch (error) {
            console.error('Error adding category:', error);
            showMessage('Erro de Conexão', 'Verifique se o backend está rodando.', 'error');
        }
    };

    const handleUpdate = async (id: number) => {
        try {
            const category = categories.find(c => c.id === id);
            if (!category) return;

            const response = await fetch(`${API_BASE}/categories/${id}`, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    name: category.name,
                    description: category.description,
                    fromFilter: category.fromFilter,
                    subjectKeywords: category.subjectKeywords,
                    bodyKeywords: category.bodyKeywords,
                    isActive: category.isActive,
                }),
            });

            if (response.ok) {
                setEditingId(null);
                await loadCategories();
                showMessage('Sucesso!', 'Categoria atualizada com sucesso.', 'success');
            } else {
                showMessage('Erro', 'Falha ao atualizar categoria.', 'error');
            }
        } catch (error) {
            console.error('Error updating category:', error);
            showMessage('Erro de Conexão', 'Verifique se o backend está rodando.', 'error');
        }
    };

    const confirmDelete = (id: number, name: string) => {
        setConfirmModal({
            isOpen: true,
            title: 'Confirmar Exclusão',
            message: `Tem certeza que deseja excluir a categoria "${name}"? Esta ação não pode ser desfeita.`,
            type: 'confirm',
            onConfirm: () => handleDelete(id),
        });
    };

    const handleDelete = async (id: number) => {
        try {
            const response = await fetch(`${API_BASE}/categories/${id}`, {
                method: 'DELETE',
            });

            if (response.ok) {
                await loadCategories();
                showMessage('Excluído!', 'Categoria removida com sucesso.', 'success');
            } else {
                showMessage('Erro', 'Falha ao excluir categoria.', 'error');
            }
        } catch (error) {
            console.error('Error deleting category:', error);
            showMessage('Erro de Conexão', 'Verifique se o backend está rodando.', 'error');
        }
    };

    const handleToggle = async (id: number) => {
        try {
            const response = await fetch(`${API_BASE}/categories/${id}/toggle`, {
                method: 'PATCH',
            });

            if (response.ok) {
                await loadCategories();
            }
        } catch (error) {
            console.error('Error toggling category:', error);
        }
    };

    const resetForm = () => {
        setFormData({
            name: '',
            description: '',
            fromFilter: '',
            subjectKeywords: '',
            bodyKeywords: '',
            isActive: true,
        });
    };

    return (
        <div className="bg-slate-800/50 backdrop-blur-sm rounded-xl border border-slate-700/50 p-6">
            {/* Custom Confirm Modal */}
            <ConfirmModal
                isOpen={confirmModal.isOpen}
                onClose={() => setConfirmModal({ ...confirmModal, isOpen: false })}
                onConfirm={confirmModal.onConfirm}
                title={confirmModal.title}
                message={confirmModal.message}
                type={confirmModal.type}
            />

            {/* Header with Action Buttons */}
            <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 mb-6">
                <div>
                    <h2 className="text-2xl font-bold text-white flex items-center gap-2">
                        ⚙️ Gerenciar Categorias
                    </h2>
                    <p className="text-gray-400 text-sm mt-1">
                        Configure filtros para monitoramento de emails
                    </p>
                </div>

                <div className="flex flex-col sm:flex-row gap-3 w-full sm:w-auto">
                    <motion.button
                        whileHover={{ scale: 1.05 }}
                        whileTap={{ scale: 0.95 }}
                        onClick={() => {
                            loadCategories();
                            setShowListModal(true);
                            setShowAddForm(false);
                        }}
                        className="w-full sm:w-auto px-4 py-2 bg-purple-600 text-white font-semibold rounded-lg hover:bg-purple-700 flex items-center justify-center gap-2"
                    >
                        <List className="w-5 h-5" />
                        Listar Categorias
                    </motion.button>

                    <motion.button
                        whileHover={{ scale: 1.05 }}
                        whileTap={{ scale: 0.95 }}
                        onClick={() => {
                            setShowAddForm(!showAddForm);
                            setShowListModal(false);
                        }}
                        className="w-full sm:w-auto px-4 py-2 bg-green-600 text-white font-semibold rounded-lg hover:bg-green-700 flex items-center justify-center gap-2"
                    >
                        {showAddForm ? <X className="w-5 h-5" /> : <Plus className="w-5 h-5" />}
                        {showAddForm ? 'Cancelar' : 'Nova Categoria'}
                    </motion.button>
                </div>
            </div>

            {/* Add Form */}
            <AnimatePresence>
                {showAddForm && (
                    <motion.div
                        initial={{ opacity: 0, height: 0 }}
                        animate={{ opacity: 1, height: 'auto' }}
                        exit={{ opacity: 0, height: 0 }}
                        className="mb-6 p-6 bg-slate-700/30 rounded-lg border border-slate-600/30"
                    >
                        <h3 className="text-lg font-semibold text-white mb-4 flex items-center gap-2">
                            <Plus className="w-5 h-5" /> Nova Categoria
                        </h3>

                        <div className="grid gap-5">
                            {/* Name & Description */}
                            <div className="grid md:grid-cols-2 gap-4">
                                <div>
                                    <label className="block text-sm font-medium text-gray-300 mb-2">
                                        Nome da Categoria *
                                    </label>
                                    <input
                                        type="text"
                                        placeholder="Ex: Full Cycle MBA"
                                        value={formData.name}
                                        onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                                        className="w-full px-4 py-2 bg-slate-600/50 text-white rounded-lg border border-slate-500/50 focus:border-blue-500 outline-none placeholder-gray-400"
                                    />
                                </div>

                                <div>
                                    <label className="block text-sm font-medium text-gray-300 mb-2">
                                        Descrição
                                    </label>
                                    <input
                                        type="text"
                                        placeholder="Ex: Alertas de aulas ao vivo"
                                        value={formData.description}
                                        onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                                        className="w-full px-4 py-2 bg-slate-600/50 text-white rounded-lg border border-slate-500/50 focus:border-blue-500 outline-none placeholder-gray-400"
                                    />
                                </div>
                            </div>

                            {/* Filters Section */}
                            <div className="p-4 bg-slate-800/50 rounded-lg border border-slate-600/20">
                                <h4 className="text-md font-medium text-blue-400 mb-4 flex items-center gap-2">
                                    <Search className="w-4 h-4" /> Filtros do Gmail
                                </h4>

                                <div className="grid gap-4">
                                    {/* From Filter */}
                                    <div>
                                        <label className="block text-sm font-medium text-gray-300 mb-2">
                                            📧 Remetente (from:)
                                        </label>
                                        <input
                                            type="text"
                                            placeholder="Ex: fullcycle.com.br"
                                            value={formData.fromFilter}
                                            onChange={(e) => setFormData({ ...formData, fromFilter: e.target.value })}
                                            className="w-full px-4 py-2 bg-slate-600/50 text-white rounded-lg border border-slate-500/50 focus:border-blue-500 outline-none placeholder-gray-400 font-mono text-sm"
                                        />
                                        <p className="text-xs text-gray-400 mt-1">
                                            Domínio ou email do remetente
                                        </p>
                                    </div>

                                    {/* Subject Keywords */}
                                    <div>
                                        <label className="block text-sm font-medium text-gray-300 mb-2">
                                            📌 Palavras no Assunto (subject:)
                                        </label>
                                        <input
                                            type="text"
                                            placeholder="Ex: AO VIVO, MBA, AGORA"
                                            value={formData.subjectKeywords}
                                            onChange={(e) => setFormData({ ...formData, subjectKeywords: e.target.value })}
                                            className="w-full px-4 py-2 bg-slate-600/50 text-white rounded-lg border border-slate-500/50 focus:border-blue-500 outline-none placeholder-gray-400 font-mono text-sm"
                                        />
                                        <p className="text-xs text-gray-400 mt-1">
                                            Separe múltiplas palavras por vírgula. Busca com OR.
                                        </p>
                                    </div>

                                    {/* Body Keywords */}
                                    <div>
                                        <label className="block text-sm font-medium text-gray-300 mb-2">
                                            📝 Palavras no Corpo do Email
                                        </label>
                                        <input
                                            type="text"
                                            placeholder="Ex: link de acesso, aula ao vivo"
                                            value={formData.bodyKeywords}
                                            onChange={(e) => setFormData({ ...formData, bodyKeywords: e.target.value })}
                                            className="w-full px-4 py-2 bg-slate-600/50 text-white rounded-lg border border-slate-500/50 focus:border-blue-500 outline-none placeholder-gray-400 font-mono text-sm"
                                        />
                                        <p className="text-xs text-gray-400 mt-1">
                                            Separe múltiplas palavras por vírgula. Busca com OR.
                                        </p>
                                    </div>
                                </div>
                            </div>

                            <button
                                onClick={handleAdd}
                                className="px-4 py-3 bg-blue-600 text-white font-semibold rounded-lg hover:bg-blue-700 transition-colors flex items-center justify-center gap-2"
                            >
                                <Plus className="w-5 h-5" /> Adicionar Categoria
                            </button>
                        </div>
                    </motion.div>
                )}
            </AnimatePresence>

            {/* List Modal - Using Portal to escape stacking context */}
            <Portal>
                <AnimatePresence>
                    {showListModal && (
                        <motion.div
                            initial={{ opacity: 0 }}
                            animate={{ opacity: 1 }}
                            exit={{ opacity: 0 }}
                            className="fixed inset-0 bg-black/60 backdrop-blur-sm flex items-center justify-center z-[80]"
                            onClick={() => setShowListModal(false)}
                        >
                            <motion.div
                                initial={{ scale: 0.9, opacity: 0 }}
                                animate={{ scale: 1, opacity: 1 }}
                                exit={{ scale: 0.9, opacity: 0 }}
                                onClick={(e) => e.stopPropagation()}
                                className="bg-slate-800 rounded-xl border border-slate-700 p-6 max-w-4xl w-full mx-4 max-h-[85vh] overflow-y-auto"
                            >
                                <div className="flex items-center justify-between mb-6">
                                    <h3 className="text-2xl font-bold text-white">📋 Categorias Cadastradas</h3>
                                    <button
                                        onClick={() => setShowListModal(false)}
                                        className="p-2 hover:bg-slate-700 rounded-lg transition-colors"
                                    >
                                        <X className="w-6 h-6 text-gray-400" />
                                    </button>
                                </div>

                                {loading && categories.length === 0 ? (
                                    <div className="text-center py-8 text-gray-400">Carregando...</div>
                                ) : categories.length === 0 ? (
                                    <div className="text-center py-8 text-gray-400">
                                        Nenhuma categoria cadastrada. Clique em "Nova Categoria" para adicionar.
                                    </div>
                                ) : (
                                    <div className="space-y-4">
                                        {categories.map((category) => (
                                            <motion.div
                                                key={category.id}
                                                initial={{ opacity: 0 }}
                                                animate={{ opacity: 1 }}
                                                className="p-4 bg-slate-700/30 rounded-lg border border-slate-600/30"
                                            >
                                                {editingId === category.id ? (
                                                    /* Editing Mode */
                                                    <div className="grid gap-4">
                                                        <div className="grid md:grid-cols-2 gap-3">
                                                            <input
                                                                type="text"
                                                                value={category.name}
                                                                placeholder="Nome"
                                                                onChange={(e) => {
                                                                    const updated = categories.map(c =>
                                                                        c.id === category.id ? { ...c, name: e.target.value } : c
                                                                    );
                                                                    setCategories(updated);
                                                                }}
                                                                className="px-3 py-2 bg-slate-600/50 text-white rounded border border-slate-500/50"
                                                            />
                                                            <input
                                                                type="text"
                                                                value={category.description}
                                                                placeholder="Descrição"
                                                                onChange={(e) => {
                                                                    const updated = categories.map(c =>
                                                                        c.id === category.id ? { ...c, description: e.target.value } : c
                                                                    );
                                                                    setCategories(updated);
                                                                }}
                                                                className="px-3 py-2 bg-slate-600/50 text-white rounded border border-slate-500/50"
                                                            />
                                                        </div>
                                                        <div className="grid md:grid-cols-3 gap-3">
                                                            <input
                                                                type="text"
                                                                value={category.fromFilter || ''}
                                                                placeholder="Remetente (from:)"
                                                                onChange={(e) => {
                                                                    const updated = categories.map(c =>
                                                                        c.id === category.id ? { ...c, fromFilter: e.target.value } : c
                                                                    );
                                                                    setCategories(updated);
                                                                }}
                                                                className="px-3 py-2 bg-slate-600/50 text-white rounded border border-slate-500/50 font-mono text-sm"
                                                            />
                                                            <input
                                                                type="text"
                                                                value={category.subjectKeywords || ''}
                                                                placeholder="Palavras no Assunto"
                                                                onChange={(e) => {
                                                                    const updated = categories.map(c =>
                                                                        c.id === category.id ? { ...c, subjectKeywords: e.target.value } : c
                                                                    );
                                                                    setCategories(updated);
                                                                }}
                                                                className="px-3 py-2 bg-slate-600/50 text-white rounded border border-slate-500/50 font-mono text-sm"
                                                            />
                                                            <input
                                                                type="text"
                                                                value={category.bodyKeywords || ''}
                                                                placeholder="Palavras no Body"
                                                                onChange={(e) => {
                                                                    const updated = categories.map(c =>
                                                                        c.id === category.id ? { ...c, bodyKeywords: e.target.value } : c
                                                                    );
                                                                    setCategories(updated);
                                                                }}
                                                                className="px-3 py-2 bg-slate-600/50 text-white rounded border border-slate-500/50 font-mono text-sm"
                                                            />
                                                        </div>
                                                        <div className="flex gap-2">
                                                            <button
                                                                onClick={() => handleUpdate(category.id)}
                                                                className="px-4 py-2 bg-green-600 text-white rounded hover:bg-green-700 flex items-center gap-2"
                                                            >
                                                                <Save className="w-4 h-4" /> Salvar
                                                            </button>
                                                            <button
                                                                onClick={() => {
                                                                    setEditingId(null);
                                                                    loadCategories();
                                                                }}
                                                                className="px-4 py-2 bg-gray-600 text-white rounded hover:bg-gray-700"
                                                            >
                                                                Cancelar
                                                            </button>
                                                        </div>
                                                    </div>
                                                ) : (
                                                    /* View Mode */
                                                    <div className="flex flex-col lg:flex-row lg:items-start justify-between gap-4">
                                                        <div className="flex-1">
                                                            <div className="flex items-center gap-3 mb-2">
                                                                <h3 className="text-lg font-semibold text-white">{category.name}</h3>
                                                                <span
                                                                    className={`px-2 py-0.5 text-xs font-medium rounded-full ${category.isActive
                                                                        ? 'bg-green-500/20 text-green-400'
                                                                        : 'bg-gray-500/20 text-gray-400'
                                                                        }`}
                                                                >
                                                                    {category.isActive ? 'Ativo' : 'Inativo'}
                                                                </span>
                                                            </div>
                                                            <p className="text-sm text-gray-400 mb-3">{category.description}</p>

                                                            {/* Filter Details */}
                                                            <div className="grid gap-2 text-sm">
                                                                {category.fromFilter && (
                                                                    <div className="flex items-center gap-2">
                                                                        <span className="text-gray-500">📧 Remetente:</span>
                                                                        <code className="text-blue-400 bg-slate-900/50 px-2 py-0.5 rounded">
                                                                            {category.fromFilter}
                                                                        </code>
                                                                    </div>
                                                                )}
                                                                {category.subjectKeywords && (
                                                                    <div className="flex items-center gap-2">
                                                                        <span className="text-gray-500">📌 Assunto:</span>
                                                                        <code className="text-yellow-400 bg-slate-900/50 px-2 py-0.5 rounded">
                                                                            {category.subjectKeywords}
                                                                        </code>
                                                                    </div>
                                                                )}
                                                                {category.bodyKeywords && (
                                                                    <div className="flex items-center gap-2">
                                                                        <span className="text-gray-500">📝 Body:</span>
                                                                        <code className="text-green-400 bg-slate-900/50 px-2 py-0.5 rounded">
                                                                            {category.bodyKeywords}
                                                                        </code>
                                                                    </div>
                                                                )}
                                                            </div>

                                                            {/* Generated Query */}
                                                            <div className="mt-3 p-2 bg-slate-900/50 rounded border border-slate-600/30">
                                                                <span className="text-xs text-gray-500">Query gerada:</span>
                                                                <code className="block text-xs text-purple-400 mt-1 font-mono break-all">
                                                                    {category.generatedQuery}
                                                                </code>
                                                            </div>
                                                        </div>

                                                        <div className="flex items-center gap-2 lg:flex-col">
                                                            <button
                                                                onClick={() => handleToggle(category.id)}
                                                                className={`p-2 rounded-lg ${category.isActive
                                                                    ? 'bg-green-600/20 text-green-400 hover:bg-green-600/30'
                                                                    : 'bg-gray-600/20 text-gray-400 hover:bg-gray-600/30'
                                                                    }`}
                                                                title={category.isActive ? 'Desativar' : 'Ativar'}
                                                            >
                                                                {category.isActive ? <Power className="w-5 h-5" /> : <PowerOff className="w-5 h-5" />}
                                                            </button>
                                                            <button
                                                                onClick={() => setEditingId(category.id)}
                                                                className="p-2 bg-blue-600/20 text-blue-400 rounded-lg hover:bg-blue-600/30"
                                                            >
                                                                <Edit2 className="w-5 h-5" />
                                                            </button>
                                                            <button
                                                                onClick={() => confirmDelete(category.id, category.name)}
                                                                className="p-2 bg-red-600/20 text-red-400 rounded-lg hover:bg-red-600/30"
                                                            >
                                                                <Trash2 className="w-5 h-5" />
                                                            </button>
                                                        </div>
                                                    </div>
                                                )}
                                            </motion.div>
                                        ))}
                                    </div>
                                )}
                            </motion.div>
                        </motion.div>
                    )}
                </AnimatePresence>
            </Portal>

            {/* Summary */}
            {!showAddForm && !showListModal && (
                <div className="text-center py-8 text-gray-400">
                    <p className="text-lg mb-2">
                        {categories.length} categoria{categories.length !== 1 ? 's' : ''} cadastrada{categories.length !== 1 ? 's' : ''}
                    </p>
                    <p className="text-sm">
                        Clique em "Listar Categorias" para visualizar e gerenciar
                    </p>
                </div>
            )}
        </div>
    );
};

export default CategoryManager;
