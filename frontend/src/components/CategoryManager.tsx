import { useState, useEffect } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { Plus, List, Edit2, Trash2, Power, PowerOff, Save, X } from 'lucide-react';
import ConfirmModal from './ConfirmModal';
import Portal from './Portal';

/**
 * Category from backend.
 */
export interface Category {
    id: number;
    name: string;
    description: string;
    emailQuery: string;
    isActive: boolean;
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
        emailQuery: '',
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
        if (!formData.name.trim() || !formData.emailQuery.trim()) {
            showMessage('Campos Obrigatórios', 'Preencha o nome e a query do Gmail.', 'error');
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
                    emailQuery: category.emailQuery,
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
            emailQuery: '',
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
                        Configure as categorias de monitoramento de emails
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
                        className="mb-6 p-4 bg-slate-700/30 rounded-lg border border-slate-600/30"
                    >
                        <h3 className="text-lg font-semibold text-white mb-4">Nova Categoria</h3>
                        <div className="grid gap-4">
                            <div>
                                <label className="block text-sm font-medium text-gray-300 mb-2">
                                    Nome da Categoria *
                                </label>
                                <input
                                    type="text"
                                    placeholder="Ex: Alerta FullCycle MBA IA"
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
                                    placeholder="Ex: Monitoramento de emails do curso"
                                    value={formData.description}
                                    onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                                    className="w-full px-4 py-2 bg-slate-600/50 text-white rounded-lg border border-slate-500/50 focus:border-blue-500 outline-none placeholder-gray-400"
                                />
                            </div>

                            <div>
                                <label className="block text-sm font-medium text-gray-300 mb-2">
                                    Query do Gmail *
                                </label>
                                <input
                                    type="text"
                                    placeholder="Ex: from:fullcycle.com.br is:unread"
                                    value={formData.emailQuery}
                                    onChange={(e) => setFormData({ ...formData, emailQuery: e.target.value })}
                                    className="w-full px-4 py-2 bg-slate-600/50 text-white rounded-lg border border-slate-500/50 focus:border-blue-500 outline-none placeholder-gray-400 font-mono text-sm"
                                />
                                <p className="text-xs text-gray-400 mt-1">
                                    Use a sintaxe de busca do Gmail. Ex: subject:importante, from:email@exemplo.com
                                </p>
                            </div>

                            <button
                                onClick={handleAdd}
                                className="px-4 py-2 bg-blue-600 text-white font-semibold rounded-lg hover:bg-blue-700 transition-colors"
                            >
                                Adicionar
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
                                className="bg-slate-800 rounded-xl border border-slate-700 p-6 max-w-4xl w-full mx-4 max-h-[80vh] overflow-y-auto"
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
                                    <div className="space-y-3">
                                        {categories.map((category) => (
                                            <motion.div
                                                key={category.id}
                                                initial={{ opacity: 0 }}
                                                animate={{ opacity: 1 }}
                                                className="p-4 bg-slate-700/30 rounded-lg border border-slate-600/30"
                                            >
                                                {editingId === category.id ? (
                                                    <div className="grid gap-3">
                                                        <input
                                                            type="text"
                                                            value={category.name}
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
                                                            onChange={(e) => {
                                                                const updated = categories.map(c =>
                                                                    c.id === category.id ? { ...c, description: e.target.value } : c
                                                                );
                                                                setCategories(updated);
                                                            }}
                                                            className="px-3 py-2 bg-slate-600/50 text-white rounded border border-slate-500/50"
                                                        />
                                                        <input
                                                            type="text"
                                                            value={category.emailQuery}
                                                            onChange={(e) => {
                                                                const updated = categories.map(c =>
                                                                    c.id === category.id ? { ...c, emailQuery: e.target.value } : c
                                                                );
                                                                setCategories(updated);
                                                            }}
                                                            className="px-3 py-2 bg-slate-600/50 text-white rounded border border-slate-500/50 font-mono text-sm"
                                                        />
                                                        <div className="flex gap-2">
                                                            <button
                                                                onClick={() => handleUpdate(category.id)}
                                                                className="px-3 py-1 bg-green-600 text-white rounded hover:bg-green-700 flex items-center gap-1"
                                                            >
                                                                <Save className="w-4 h-4" /> Salvar
                                                            </button>
                                                            <button
                                                                onClick={() => setEditingId(null)}
                                                                className="px-3 py-1 bg-gray-600 text-white rounded hover:bg-gray-700"
                                                            >
                                                                Cancelar
                                                            </button>
                                                        </div>
                                                    </div>
                                                ) : (
                                                    <div className="flex items-center justify-between">
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
                                                            <p className="text-sm text-gray-400 mb-1">{category.description}</p>
                                                            <code className="text-xs text-blue-400 bg-slate-900/50 px-2 py-1 rounded">
                                                                {category.emailQuery}
                                                            </code>
                                                        </div>

                                                        <div className="flex items-center gap-2">
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
